package com.example.StockFetcher.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class EnhancedYahooFinanceService {
    private final YahooFinanceService yahooService;
    private final CacheService cacheService;

    // Configurações
    @Value("${app.api.max-parallel-requests}")
    private int maxParallelRequests;

    @Value("${app.api.request-delay-ms}")
    private int requestDelayMs;

    @Value("${app.cache.stale-threshold}")
    private long staleThresholdMinutes;

    @Autowired private ServiceRegistry serviceRegistry;

    private static final int BATCH_SIZE = 20;
    private static final int BATCH_DELAY_MS = 2000;

    /**
     * Método principal para o frontend - resposta rápida com cache
     */
    public PortfolioSnapshot getPortfolioSnapshot(List<String> symbols) {

        if (serviceRegistry.getStatus("stockFetcher") != ServiceStatus.ATIVO) {
            throw new IllegalStateException("Serviço stockFetcher desativado manualmente");
        }

        Map<String, String> cachedData = cacheService.getFullPortfolioCache(symbols);
        List<String> staleSymbols = cacheService.getMissingSymbols(symbols, staleThresholdMinutes);

        if (!staleSymbols.isEmpty()) {
            asyncRefreshStocks(staleSymbols);
        }

        return PortfolioSnapshot.builder()
                .data(cachedData)
                .staleSymbols(staleSymbols)
                .status(cachedData.size() == symbols.size() ? "COMPLETE" : "PARTIAL")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Método para serviços internos (com retry)
     */
    @Retryable(value = {ResourceAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000))
    @RateLimiter(name = "yahooRateLimiter")
    public Map<String, String> fetchWithRetry(List<String> symbols) {

        return fetchWithThrottling(symbols);
    }

    /**
     * Atualização assíncrona
     */
    @Async
    public void asyncRefreshStocks(List<String> symbols) {
        try {
            Map<String, String> freshData = fetchWithThrottling(symbols);
            freshData.forEach((symbol, data) -> {
                if (!data.contains("\"error\"")) {
                    cacheService.updateCache(Collections.singletonMap(symbol, data));
                    log.debug("Atualizado cache para: {}", symbol);
                }
            });
        } catch (Exception e) {
            log.error("Falha na atualização assíncrona", e);
        }
    }

    /**
     * Busca com throttling controlado
     */
    private Map<String, String> fetchWithThrottling(List<String> symbols) {

        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyMap();
        }

        ExecutorService executor = Executors.newFixedThreadPool(maxParallelRequests);
        Map<String, String> results = new ConcurrentHashMap<>();

        try {
            List<CompletableFuture<Void>> futures = symbols.stream()
                    .map(symbol -> CompletableFuture.runAsync(() -> {
                        try {
                            results.put(symbol, yahooService.getStockPrice(symbol));
                            Thread.sleep(requestDelayMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } catch (Exception e) {
                            results.put(symbol, "{\"error\":\"" + e.getMessage() + "\"}");
                        }
                    }, executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        return results;
    }

    /**
     * Atualização periódica em background
     */
    @Scheduled(fixedRate = 2 * 60 * 1000) // 2 minutos
    public void scheduledBatchUpdate() {
        List<String> staleSymbols = cacheService.getStaleSymbols(staleThresholdMinutes);

        if (!staleSymbols.isEmpty()) {
            log.info("Starting update for {} stale symbols", staleSymbols.size());

            // Processa em lotes para evitar sobrecarga
            for (int i = 0; i < staleSymbols.size(); i += BATCH_SIZE) {
                List<String> batch = staleSymbols.subList(i, Math.min(i + BATCH_SIZE, staleSymbols.size()));
                Map<String, String> freshData = fetchWithThrottling(batch);
                cacheService.updateCache(freshData);

                try {
                    Thread.sleep(BATCH_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
    @Scheduled(fixedRate = 30*60*1000) // 30 minutos
    public void scheduledCacheCleanup(){
        List<String> staleSymbols = cacheService.getStaleSymbols(staleThresholdMinutes);
        if (!staleSymbols.isEmpty()) {
            cacheService.evictStaleEntries(staleThresholdMinutes);
            log.info("Limpeza de cache: {} entradas removidas", staleSymbols.size());
        }
    }

    /**
     * DTO para resposta ao frontend
     */
    @Data
    @Builder
    public static class PortfolioSnapshot {
        private Map<String, String> data;
        private List<String> staleSymbols;
        private String status; // COMPLETE, PARTIAL
        private long timestamp;
    }

}