package com.example.StockFetcher.service;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class YahooFinanceService {

    private RestTemplate restTemplate;
    private final String apiURL = "https://query1.finance.yahoo.com/v8/finance/chart/{symbol}.SA?interval=1d&range=1mo&events=div";
    @Autowired private CacheService cacheService;
    @Autowired private ServiceRegistry serviceRegistry;

    public YahooFinanceService(){
        this.restTemplate = new RestTemplate();
        this.restTemplate.getInterceptors().add(((request, body, execution) -> {
            request.getHeaders().set("User-Agent", "Mozilla/5.0");
            return execution.execute(request,body);
        }));
    }
    @Cacheable(value = "stockCache",key = "#symbol", unless = "#result ==null")
    @RateLimiter(name = "yahooFinanceLimiter", fallbackMethod = "fallback")
    public String getStockPrice(String symbol){


        String url = apiURL.replace("{symbol}",symbol);
        ResponseEntity<String> response = restTemplate.getForEntity(url,String.class);

        return cacheService.addTimestamp(response.getBody());
    }

    public String fallback(String symbol, RequestNotPermitted ex) {
        return "{\"error\":\"rate-limited\",\"symbol\":\"" + symbol + "\"}";
    }


}
