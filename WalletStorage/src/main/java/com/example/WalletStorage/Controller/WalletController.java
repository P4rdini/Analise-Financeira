package com.example.WalletStorage.Controller;

import com.example.WalletStorage.Model.Wallet;
import com.example.WalletStorage.Service.ServiceRegistry;
import com.example.WalletStorage.Service.ServiceStatus;
import com.example.WalletStorage.exception.ServiceUnavailableException;
import com.example.WalletStorage.Service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/wallets")
public class WalletController {
    @Autowired
    WalletService walletService;
    @Autowired private ServiceRegistry serviceRegistry;

    // verificar status dos microserviços
    @GetMapping("/status")
    public ResponseEntity<Map<String, ServiceStatus>> getServiceStatuses(){
        return ResponseEntity.ok(serviceRegistry.getAllStatuses());
    }
    @PostMapping("/toggle-service")
    public ResponseEntity<Void> toggleService(@RequestParam String service){
        ServiceStatus current = serviceRegistry.getStatus(service);
        ServiceStatus newStatus = current == ServiceStatus.ATIVO ? ServiceStatus.INATIVO : ServiceStatus.ATIVO;
        serviceRegistry.setServiceStatus(service,newStatus);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{userId}/buy")
    public ResponseEntity<Wallet> buyAsset
            (@PathVariable String userId,
             @RequestParam String symbol,
             @RequestParam int quantity,
             @RequestParam double price){

        if (serviceRegistry.getStatus("walletStorage") != ServiceStatus.ATIVO) {
            throw new ServiceUnavailableException("walletStorage");        }

        Wallet wallet = walletService.buyAsset(userId,symbol,quantity,price);
        return ResponseEntity.ok(wallet);
    }


    @GetMapping("/{userId}")
    public ResponseEntity<Wallet> getWallet(@PathVariable String userId){
        if (serviceRegistry.getStatus("walletStorage") != ServiceStatus.ATIVO) {
            throw new ServiceUnavailableException("walletStorage");
        }
        return walletService.getWallet(userId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{userId}/sell")
    public ResponseEntity<Wallet> sellAsset(@PathVariable String userId,
                                            @RequestParam String symbol,
                                            @RequestParam int quantity,
                                            @RequestParam double price){
        if (serviceRegistry.getStatus("walletStorage") != ServiceStatus.ATIVO) {
            throw new ServiceUnavailableException("walletStorage");
        }

        Wallet wallet = walletService.sellAsset(userId, symbol, quantity, price);
        return ResponseEntity.ok(wallet);
    }


    @GetMapping("/{userId}/transactions")
    public ResponseEntity<?> getTransactions(@PathVariable String userId){
        if (serviceRegistry.getStatus("walletStorage") != ServiceStatus.ATIVO) {
            throw new ServiceUnavailableException("walletStorage");
        }
        return walletService.getWallet(userId)
                .map(wallet -> ResponseEntity.ok(wallet.getTransactions()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}/assets")
    public ResponseEntity<?> getAssets(@PathVariable String userId){
        if (serviceRegistry.getStatus("walletStorage") != ServiceStatus.ATIVO) {
            throw new ServiceUnavailableException("walletStorage");
        }

        return walletService.getWallet(userId)
                .map(wallet -> ResponseEntity.ok(wallet.getAssets()))
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/create")
    public ResponseEntity<Void> createWallet(@RequestParam String userId){
        Wallet wallet = walletService.createWallet(userId);
        return ResponseEntity.ok().build();
    }



}
