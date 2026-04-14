package com.example.Controle_Financeiro.DTO;

import lombok.Data;

import java.util.List;

@Data
public class StockDTO {

    private String symbol;
    private double currentPrice;
    private long quantity;
    private double avgPrice;
    private double retornoDiario;
    private double sma;
    private double rsi;
    private List<Double> bollinger;
    private double volumeMedio;
    private double lucroPrejuizo;
    private double rentabilidade;
}
