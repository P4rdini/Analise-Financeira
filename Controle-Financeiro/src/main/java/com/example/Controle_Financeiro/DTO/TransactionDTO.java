package com.example.Controle_Financeiro.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class TransactionDTO {
    private String symbol;
    private String type;
    private double price;
    private long quantity;
    private LocalDateTime date;
}
