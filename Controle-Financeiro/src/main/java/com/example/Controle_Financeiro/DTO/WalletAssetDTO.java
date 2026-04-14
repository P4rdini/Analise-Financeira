package com.example.Controle_Financeiro.DTO;

import lombok.Data;

@Data
public class WalletAssetDTO {
    private long id;
    private String symbol;
    private int quantity;
    private Double avgPrice;


}
