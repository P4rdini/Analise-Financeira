package com.example.WalletProcessor.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class StockSnapshotResponse {

        private Map<String, String> data;
        private List<String> staleSymbols;
        private String status; // COMPLETE, PARTIAL
        private long timestamp;

}
