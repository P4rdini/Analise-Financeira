package com.example.WalletStorage.Model;

import jakarta.persistence.*;
import org.apache.el.parser.AstSetData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Entity
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String userId;
    private double balance;
    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "wallet")
    private List<Asset> assets = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "wallet")
    private List<Transaction> transactions = new ArrayList<>();


    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userID) {
        this.userId = userID;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }


}

