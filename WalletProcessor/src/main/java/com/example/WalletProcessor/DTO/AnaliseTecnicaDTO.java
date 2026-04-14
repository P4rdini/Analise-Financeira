package com.example.WalletProcessor.DTO;

import lombok.Data;

@Data
public class AnaliseTecnicaDTO {

    private String symbol;
    private double currentPrice;
    private long quantity;
    private double avgPrice;
    private double retornoDiario;
    private double sma;
    private double rsi;
    private double[] bollinger;
    private double volumeMedio;
    private double lucroPrejuizo;
    private double rentabilidade;
}
