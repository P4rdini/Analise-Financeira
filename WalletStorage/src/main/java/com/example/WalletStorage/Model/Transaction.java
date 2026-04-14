package com.example.WalletStorage.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String symbol;
    private String type; //buy or sell
    private double price;
    private int quantity;
    private LocalDateTime date;
    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    public Transaction(String symbol, String type, double price, int quantity, LocalDateTime date) {
        this.symbol = symbol;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.date = date;
    }

    public Transaction() {
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
