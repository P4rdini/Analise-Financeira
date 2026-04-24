package com.example.WalletStorage.Controller;

import com.example.WalletStorage.Model.AssetAutoComplete;
import com.example.WalletStorage.Service.ServiceAssetAutoComplete;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetAutoCompleteController {
    @Autowired private ServiceAssetAutoComplete service;

    @GetMapping("/autocomplete")
        public ResponseEntity<List<AssetAutoComplete>> getAutoComplete(@RequestParam String query){
        if(query == null ||query.trim().isEmpty()){
           return ResponseEntity.ok(List.of());
        }
        List<AssetAutoComplete> result = service.searchSymbol(query);
        System.out.println("retorno do banco "+result.toString());
        return ResponseEntity.ok(result);
    }
}
