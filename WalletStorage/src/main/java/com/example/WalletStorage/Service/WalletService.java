package com.example.WalletStorage.Service;

import com.example.WalletStorage.Model.Asset;
import com.example.WalletStorage.Model.Transaction;
import com.example.WalletStorage.Model.Wallet;
import com.example.WalletStorage.Repository.WalletRespository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WalletService{
    @Autowired
    private WalletRespository walletRespository;

    @Transactional
    public Wallet buyAsset(String userId,String symbol,int quanlity, double price){
        Wallet wallet = walletRespository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Carteira do usuario "+userId+" não encontrada"));

        Optional<Asset> existngAsset = wallet.getAssets().stream()
                .filter(a -> a.getSymbol().equals(symbol)).findFirst();

        if(existngAsset.isPresent()){
            Asset asset = existngAsset.get();
            double newAvgPrice = ((asset.getAvgPrice() * asset.getQuantity()) + (price*quanlity)) / (asset.getQuantity() + quanlity);
            asset.setQuantity(asset.getQuantity() + quanlity);
            asset.setAvgPrice(newAvgPrice);
        } else {
            wallet.getAssets().add(new Asset(symbol,quanlity,price));
        }

        wallet.setBalance(wallet.getBalance() - (price * quanlity));

        wallet.getTransactions().add(new Transaction(symbol,"BUY",price,quanlity, LocalDateTime.now()));

        return walletRespository.save(wallet);
    }

    @Transactional
    public Wallet sellAsset(String userId,String symbol,int quantity,double price){
        Wallet wallet = walletRespository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Carteira nao encontrada"));
        Asset asset = wallet.getAssets().stream().filter(a -> a.getSymbol().equals(symbol))
                .findFirst().orElseThrow(() -> new RuntimeException("Ativo nao encontrado"));
        if(asset.getQuantity() < quantity){
            throw new RuntimeException("Quantidade insuficiente para venda");
        }

        asset.setQuantity(asset.getQuantity() - quantity);

        wallet.setBalance(wallet.getBalance() + (price * quantity));

        if(asset.getQuantity() ==0 ){
            wallet.getAssets().remove(asset);
        }
        wallet.getTransactions().add(new Transaction(symbol, "SELL", price, quantity, LocalDateTime.now()));

        return walletRespository.save(wallet);

    }

    public Optional<Wallet> getWallet(String userId) {
        return walletRespository.findByUserId(userId);
    }

    public Wallet createWallet(String userId) {
        if(walletRespository.findByUserId(userId).isPresent()){
            throw new RuntimeException("Ja existe uma carteira para esse usuario");
        }
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(0.0);
        return walletRespository.save(wallet);
    }
}
