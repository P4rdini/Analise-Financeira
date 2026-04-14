package com.example.StockFetcher.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;



    /**
     * Obtém todos os símbolos do cache
     */
    public Map<String, String> getFullPortfolioCache(List<String> symbols) {
        Cache cache = getCache();
        Map<String, String> result = new HashMap<>();

        symbols.forEach(symbol -> {
            String cachedValue = getFromCache(cache, symbol);
            if (cachedValue != null) {
                result.put(symbol, cachedValue);
            }
        });

        return result;
    }

    /**
     * Identifica símbolos faltantes ou desatualizados
     */
    public List<String> getMissingSymbols(List<String> symbols, long staleThresholdMinutes) {
        Cache cache = getCache();
        List<String> missingSymbols = new ArrayList<>();
        long staleThreshold = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(staleThresholdMinutes);

        symbols.forEach(symbol -> {
            String cachedValue = getFromCache(cache, symbol);
            if (cachedValue == null || isStale(cachedValue, staleThreshold)) {
                missingSymbols.add(symbol);
            }
        });

        return missingSymbols;
    }

    /**
     * Atualiza o cache com novos dados
     */
    public void updateCache(Map<String, String> freshData) {
        if (freshData == null || freshData.isEmpty()) return;

        Cache cache = getCache();
        freshData.forEach((symbol, data) -> {
            String enrichedData = addTimestamp(data);
            cache.put(symbol, enrichedData);
        });
    }

    /**
     * Obtém símbolos desatualizados
     */
    public List<String> getStaleSymbols(long staleThresholdMinutes) {
        Cache cache = getCache();
        List<String> staleSymbols = new ArrayList<>();
        long staleThreshold = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(staleThresholdMinutes);

        if (cache != null) {
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                    (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();

            nativeCache.asMap().forEach((key, value) -> {
                try {
                    if (isStale((String) value, staleThreshold)) {
                        staleSymbols.add((String) key);
                    }
                } catch (Exception e) {
                    log.warn("Failed to check staleness for {}", key, e);
                }
            });
        }

        return staleSymbols;
    }

    /**
     * Métodos auxiliares privados
     */
    private Cache getCache() {
        return cacheManager.getCache("stockCache");
    }

    private String getFromCache(Cache cache, String symbol) {
        return cache != null ? cache.get(symbol, String.class) : null;
    }

    private boolean isStale(String cachedValue, long staleThreshold) {
        try {
            JsonNode jsonNode = objectMapper.readTree(cachedValue);
            long lastUpdated = jsonNode.path("timestamp").asLong(0);
            return lastUpdated < staleThreshold;
        } catch (Exception e) {
            log.warn("Invalid cache format", e);
            return true; // Considera como desatualizado se não puder verificar
        }
    }

    public String addTimestamp(String jsonData) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonData);
            ((com.fasterxml.jackson.databind.node.ObjectNode) rootNode)
                    .put("timestamp", System.currentTimeMillis());
            return rootNode.toString();
        } catch (Exception e) {
            log.warn("Failed to add timestamp to cache data", e);
            return jsonData;
        }
    }

    /**
     * Métodos adicionais para monitoramento
     */
    public CacheStats getCacheStats() {
        Cache cache = getCache();
        if (cache != null) {
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                    (com.github.benmanes.caffeine.cache.Cache<Object, Object>) cache.getNativeCache();
            return nativeCache.stats();
        }
        return null;
    }

    public void evictStaleEntries(long staleThresholdMinutes) {
        getStaleSymbols(staleThresholdMinutes).forEach(symbol -> {
            Cache cache = getCache();
            if (cache != null) {
                cache.evict(symbol);
            }
        });
    }

}
