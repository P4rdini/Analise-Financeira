package com.example.Controle_Financeiro.Client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "Stock-Fetcher",url = "${stockfetcher.url}")
public interface StockFetcherClient {
    @GetMapping("/api/stocks/price")
    String getPrice(@RequestParam("symbol") String symbol);

    @GetMapping("/api/stocks/with-retry")
    Map<String, String> getStocksWithRetry(@RequestParam("symbols") List<String> symbols);

    @GetMapping("/api/stocks/fresh-data")
    Map<String, String>  getSnapshotDate(@RequestParam("symbols") String symbols);
}
