package com.example.WalletProcessor.Service;

import com.example.WalletProcessor.Model.StockData;

import java.util.ArrayList;
import java.util.List;

public class AnaliseTecnicaService {

    private final List<Double> fechamentos;
    private final List<Long> volumes;

    public AnaliseTecnicaService(StockData stockData) {
        this.fechamentos = stockData.getClose();
        this.volumes = stockData.getVolume();
    }

    // 1. retorno diario (%)
    public double calcularRetornoDiario(){
        if(fechamentos.size() < 2) return 0.0;
        double precoAtual = fechamentos.get(fechamentos.size()-1);
        double precoAlterior = fechamentos.get(fechamentos.size()-2);
        return ((precoAtual-precoAlterior) *100);
    }

    // 2. Media Movel Simples (SMA)
    public double calcularSMA(int periodo){
        if (fechamentos.size() < periodo) return 0.0;
        double soma = fechamentos.stream()
                .skip(fechamentos.size() - periodo)
                .mapToDouble(Double::doubleValue)
                .sum();
        return soma / periodo;
    }

    // 3. Indice de Força Relativa (RSI)
    public double calcularRSI(int periodo){
        // Validações iniciais
        if (fechamentos == null || fechamentos.size() < 2 || periodo <= 0) {
            return 50.0; // Valor neutro para dados inválidos
        }

        List<Double> ganhos = new ArrayList<>();
        List<Double> perdas = new ArrayList<>();

        // Calcula variações diárias
        for (int i = 1; i < fechamentos.size(); i++) {
            double variacao = fechamentos.get(i) - fechamentos.get(i - 1);
            if (variacao > 0) {
                ganhos.add(variacao);
            } else {
                perdas.add(Math.abs(variacao)); // Perdas são valores absolutos
            }
        }

        // Médias dos ganhos e perdas (apenas últimos 'periodo' dias)
        double mediaGanhos = ganhos.stream().limit(periodo).mapToDouble(Double::doubleValue).average().orElse(0.0);
        double mediaPerdas = perdas.stream().limit(periodo).mapToDouble(Double::doubleValue).average().orElse(0.0);


        if (mediaPerdas == 0) {
            return (mediaGanhos == 0) ? 50.0 : 100.0; // Se não houve perdas, RSI = 100
        }

        double rs = mediaGanhos / mediaPerdas;
        double rsi = 100 - (100 / (1 + rs));


        return Math.max(0, Math.min(100, rsi));
    }

    // 4. Bandas de Bollinger
    public double[] calcularBandasBollinger(int periodo, int multiplicador){
        double sma = calcularSMA(periodo);
        double somaQuadrados = fechamentos.stream()
                .skip(fechamentos.size() - periodo)
                .mapToDouble(preco -> Math.pow(preco - sma, 2))
                .sum();

        double desvioPadrao = Math.sqrt(somaQuadrados / periodo);
        return new double[]{
                sma + (multiplicador * desvioPadrao), // Banda Superior
                sma,                                   // SMA
                sma - (multiplicador * desvioPadrao)   // Banda Inferior
        };
    }

    // 5. Volume Médio
    public double calcularVolumeMedio(int periodo){
        if(volumes.size() < periodo) return 0.0;
        return volumes.stream()
                .skip(volumes.size() - periodo)
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }
}
