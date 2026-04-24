package com.example.WalletStorage.Service;

import com.example.WalletStorage.Model.AssetAutoComplete;
import com.example.WalletStorage.Repository.AssetAutoCompleteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceAssetAutoComplete {
    @Autowired
    private AssetAutoCompleteRepository repository;

    public List<AssetAutoComplete> searchSymbol(String symbol){
        return repository.findTop10BySymbolContainingIgnoreCase(symbol);
    }
}
