package com.example.StockFetcher.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class StockSnapshotResponse {
    private Map<String, String> data;       // Símbolo -> JSON de dados
    private List<String> staleSymbols;      // Símbolos desatualizados
    private String status;                  // "COMPLETE" ou "PARTIAL"
    private long timestamp;
}
