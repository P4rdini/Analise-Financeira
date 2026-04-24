package com.example.Controle_Financeiro.DTO;

import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
public class CarteiraDTO {
    List<Map<String, Object>> stocksComRentabilidade;
    double totalInvestido;
    double totalAtual;
    double rentabilidadeTotal;
    double retornoDiariototal;
    double lucroTotal;
}
