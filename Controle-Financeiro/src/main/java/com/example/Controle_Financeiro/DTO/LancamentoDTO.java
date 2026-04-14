package com.example.Controle_Financeiro.DTO;

import lombok.Data;

@Data
public class LancamentoDTO {
    private String username;
    private String operacao;
    private String acao;
    private int quantidade;
    private double valor;

    public String getAcao() {
        return acao.toUpperCase();
    }
}
