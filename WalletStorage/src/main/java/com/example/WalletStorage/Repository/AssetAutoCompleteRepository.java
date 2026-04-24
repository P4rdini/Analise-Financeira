package com.example.WalletStorage.Repository;

import com.example.WalletStorage.Model.AssetAutoComplete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetAutoCompleteRepository extends JpaRepository<AssetAutoComplete,Long> {
    Optional<AssetAutoComplete> findBySymbol (String symbol);
    List<AssetAutoComplete> findTop10BySymbolContainingIgnoreCase(String symbol);
}
