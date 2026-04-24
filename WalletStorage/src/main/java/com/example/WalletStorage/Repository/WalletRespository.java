package com.example.WalletStorage.Repository;

import com.example.WalletStorage.Model.AssetAutoComplete;
import com.example.WalletStorage.Model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface WalletRespository extends JpaRepository<Wallet,String> {
    Optional<Wallet> findByUserId(String userId);
}
