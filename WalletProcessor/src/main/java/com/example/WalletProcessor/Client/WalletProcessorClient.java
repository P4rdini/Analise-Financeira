package com.example.WalletProcessor.Client;

import com.example.WalletProcessor.DTO.StockSnapshotResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "Stock-Fetcher",url = "${stockfetcher.url}")
public interface WalletProcessorClient {

    @GetMapping("/api/stocks/price")
    String getPrice(@RequestParam("symbol") String symbol);

    @GetMapping("/api/stocks/with-retry")
    Map<String, String> getStocksWithRetry(@RequestParam("symbols") List<String> symbols);

        @GetMapping("/api/stocks/fresh-data")
        Map<String, String>  getSnapshotDate(@RequestParam("symbols") String symbols);


}


