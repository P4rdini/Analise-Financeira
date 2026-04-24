package com.example.Controle_Financeiro.Service;

import com.example.Controle_Financeiro.Client.StockFetcherClient;
import com.example.Controle_Financeiro.DTO.CarteiraDTO;
import com.example.Controle_Financeiro.DTO.SnapshotRequestDTO;
import com.example.Controle_Financeiro.DTO.StockDTO;
import com.example.Controle_Financeiro.DTO.WalletAssetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WalletService {
    @Autowired private RestTemplate restTemplate;
    @Autowired private StockFetcherClient stockFetcherClient;
    private String urlStorage = "http://localhost:8083/wallets/";

    public CarteiraDTO montarCarteira(String username,SequencedCollection<StockDTO> acoes){


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SequencedCollection<StockDTO>> entity = new HttpEntity<>(acoes, headers);
        ResponseEntity<CarteiraDTO> response = restTemplate.postForEntity(
                "http://localhost:8082/test/snapshot/"+ username +"/carteira",
                entity,
                CarteiraDTO.class
        );
        return response.getBody();
    }

    public List<StockDTO> getAçõesCarteira(String username) throws Exception{
        String walletStorageUrl = urlStorage + username + "/assets";


        WalletAssetDTO[] symbolsDTO = restTemplate.getForObject(walletStorageUrl,WalletAssetDTO[].class);
        List<String> symbols = Arrays.stream(symbolsDTO).map(WalletAssetDTO::getSymbol).collect(Collectors.toList());
        Map<String, String> dadosAções = stockFetcherClient.getSnapshotDate(String.join(",",symbols));

        List<StockDTO> stocks = new ArrayList<>();

        SnapshotRequestDTO requestDTO = new SnapshotRequestDTO();
        requestDTO.setDadosAcoes(dadosAções);
        requestDTO.setSymbols(List.of(symbolsDTO));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SnapshotRequestDTO> entity = new HttpEntity<>(requestDTO, headers);

        ResponseEntity<StockDTO[]> response = restTemplate.postForEntity(
                "http://localhost:8082/test/snapshot/" + username,
                entity,
                StockDTO[].class
        );
        if(response.getBody() != null){
            stocks = Arrays.asList(response.getBody());
        }
        return stocks;
    }

}
