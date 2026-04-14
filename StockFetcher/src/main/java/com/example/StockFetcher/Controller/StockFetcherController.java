package com.example.StockFetcher.Controller;

import com.example.StockFetcher.exception.ServiceUnavailableException;
import com.example.StockFetcher.service.*;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockFetcherController {
    @Autowired private final EnhancedYahooFinanceService enhancedYahooFinanceService;
    @Autowired private final YahooFinanceService yahooFinanceService;
    @Autowired private CacheService cacheService;
    @Value("${app.cache.stale-threshold-minutes}") private long staleThresholdMinutes;
    @Autowired private ServiceRegistry serviceRegistry;


    // verificar status dos microserviços
    @GetMapping("/status")
    public ResponseEntity<Map<String, ServiceStatus>> getServiceStatuses(){
        return ResponseEntity.ok(serviceRegistry.getAllStatuses());
    }
    @PostMapping("/toggle-service")
    public ResponseEntity<Void> toggleService(@RequestParam String service){
        ServiceStatus current = serviceRegistry.getStatus(service);
        ServiceStatus newStatus = current == ServiceStatus.ATIVO ? ServiceStatus.INATIVO : ServiceStatus.ATIVO;
        serviceRegistry.setServiceStatus(service,newStatus);
        return ResponseEntity.ok().build();
    }



    /**
     * Endpoint para outros serviços que precisam de dados frescos (com retry)
     */
    @Retry(name = "stockRetry")
    @RateLimiter(name = "stockRateLimiter")
    @GetMapping("/fresh-data")
    public ResponseEntity<Map<String, String>> getFreshStockData(@RequestParam List<String> symbols) {
        if (serviceRegistry.getStatus("stockFetcher") != ServiceStatus.ATIVO) {
            throw new ServiceUnavailableException("stockFetcher");
        }
        log.debug("Fetching fresh data for symbols: {}", symbols);
        return ResponseEntity.ok(enhancedYahooFinanceService.fetchWithRetry(symbols));
    }

    /**
     * Endpoint original mantido para compatibilidade
     */
    @GetMapping("/price")
    public ResponseEntity<String> getSinglePrice(@RequestParam String symbol) {
        log.debug("Fetching single price for symbol: {}", symbol);
        return ResponseEntity.ok(yahooFinanceService.getStockPrice(symbol));
    }

    /**
     * Endpoint para monitoramento do cache
     */
    @GetMapping("/cache-info")
    public ResponseEntity<Map<String, Object>> getCacheInfo() {
        CacheStats stats = cacheService.getCacheStats();
        if(stats ==null){
            return ResponseEntity.ok(Map.of("status","Cache nao inicializado"));
        }
        return ResponseEntity.ok(Map.of(
                "hitRate",stats.hitRate(),
                "missRate",stats.missRate(),
                "evictionCount",stats.evictionCount(),
                "everageLoadPenalty",stats.averageLoadPenalty()));
    }

    /**
     * Endpoint para forçar atualização de símbolos específicos
     */
    @GetMapping("/refresh")
    public ResponseEntity<Void> refreshStocks(@RequestParam List<String> symbols) {
        log.info("Manual refresh requested for symbols: {}", symbols);
        enhancedYahooFinanceService.asyncRefreshStocks(symbols);
        return ResponseEntity.accepted().build();
    }
}
