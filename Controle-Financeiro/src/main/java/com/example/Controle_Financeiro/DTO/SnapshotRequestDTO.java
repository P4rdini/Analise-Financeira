package com.example.Controle_Financeiro.DTO;

import java.util.List;
import java.util.Map;

public class SnapshotRequestDTO {

    private Map<String, String> dadosAcoes;
    private List<WalletAssetDTO> symbols;

    public Map<String, String> getDadosAcoes() {
        return dadosAcoes;
    }

    public void setDadosAcoes(Map<String, String> dadosAcoes) {
        this.dadosAcoes = dadosAcoes;
    }

    public List<WalletAssetDTO> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<WalletAssetDTO> symbols) {
        this.symbols = symbols;
    }
}
