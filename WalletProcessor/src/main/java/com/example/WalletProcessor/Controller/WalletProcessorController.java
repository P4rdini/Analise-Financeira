package com.example.WalletProcessor.Controller;

import com.example.WalletProcessor.Client.WalletProcessorClient;
import com.example.WalletProcessor.DTO.AnaliseTecnicaDTO;
import com.example.WalletProcessor.DTO.CarteiraDTO;
import com.example.WalletProcessor.DTO.SnapshotRequestDTO;
import com.example.WalletProcessor.DTO.WalletAssetDTO;
import com.example.WalletProcessor.Model.StockData;
import com.example.WalletProcessor.Service.AnaliseTecnicaService;
import com.example.WalletProcessor.Service.ServiceRegistry;
import com.example.WalletProcessor.Service.ServiceStatus;
import com.example.WalletProcessor.Service.StockDataParser;
import com.example.WalletProcessor.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/test")
public class WalletProcessorController {
    @Autowired
    WalletProcessorClient client;

    @Autowired private RestTemplate restTemplate;
    @Value("${controle.financeiro.url}") private String controleFinanceiroUrl;
    private String urlStorage = "http://localhost:8083/wallets/";
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
    // metodo para pegar as ações da carteira no microserviço walletStorage
    @PostMapping("/snapshot/{userId}")
    public List<AnaliseTecnicaDTO> getsnapshot(@PathVariable String userId,@RequestBody SnapshotRequestDTO requestDTO) {

        if (serviceRegistry.getStatus("walletProcessor") != ServiceStatus.ATIVO) {
            throw new ServiceUnavailableException("walletProcessor");
        }

        Map<String, AnaliseTecnicaDTO> dtoMap = new HashMap<>();
        requestDTO.getDadosAcoes().forEach((chave,valor) ->{
            try {

                StockData data = StockDataParser.parseJson(valor);
                AnaliseTecnicaService analise = new AnaliseTecnicaService(data);
                dtoMap.put(chave, createAnaliseTecnicaDTO(chave, data, analise));

            } catch (Exception e) {
                throw new RuntimeException("Erro ao processar dados de " + chave, e);
            }
        });

        for (WalletAssetDTO assetDTO : requestDTO.getSymbols()) {

            String key = assetDTO.getSymbol().toUpperCase();
            AnaliseTecnicaDTO dto = dtoMap.get(key);
            if (dto != null) {
                dto.setQuantity(assetDTO.getQuantity());
                dto.setAvgPrice(assetDTO.getAvgPrice());
                double valorInvestido = assetDTO.getAvgPrice() * assetDTO.getQuantity();
                double valorAtual = dto.getCurrentPrice() * assetDTO.getQuantity();
                double lucroPrejuizo = valorAtual - valorInvestido;
                double rentabilidade = (lucroPrejuizo / valorInvestido) * 100;
                dto.setRentabilidade(rentabilidade);
                dto.setLucroPrejuizo(lucroPrejuizo);
            }
        }


        return new ArrayList<>(dtoMap.values());
    }

    @PostMapping("/snapshot/{userId}/carteira")
    public CarteiraDTO getMinhaCarteira(@PathVariable String userId,@RequestBody List<AnaliseTecnicaDTO> acoes){
        if (serviceRegistry.getStatus("walletProcessor") != ServiceStatus.ATIVO) {
            throw new ServiceUnavailableException("walletProcessor");
        }


        double totalInvestido = 0;
        double totalAtual = 0;
        double retornoDiarioTotal = 0;
        double lucroTotal = 0;
        List<Map<String, Object>> stocksComRentabilidade = new ArrayList<>();

        for (AnaliseTecnicaDTO stock : acoes) {
            double valorInvestido = stock.getAvgPrice() * stock.getQuantity();
            double valorAtual = stock.getCurrentPrice() * stock.getQuantity();
            double lucroPrejuizo = valorAtual - valorInvestido;
            double rentabilidade = valorInvestido > 0 ? (lucroPrejuizo / valorInvestido) * 100 : 0;

            retornoDiarioTotal += stock.getRetornoDiario();

            Map<String, Object> stockData = new HashMap<>();
            stockData.put("stock", stock);
            stockData.put("valorInvestido", valorInvestido);
            stockData.put("valorAtual", valorAtual);
            stockData.put("lucroPrejuizo", lucroPrejuizo);
            stockData.put("rentabilidade", rentabilidade);

            stocksComRentabilidade.add(stockData);

            totalInvestido += valorInvestido;
            totalAtual += valorAtual;
        }
        lucroTotal = totalAtual - totalInvestido;
        double rentabilidadeTotal = totalInvestido > 0 ? ((totalAtual - totalInvestido) / totalInvestido) * 100 : 0;
        CarteiraDTO carteira = new CarteiraDTO();
        carteira.setTotalAtual(totalAtual);
        carteira.setTotalInvestido(totalInvestido);
        carteira.setRentabilidadeTotal(rentabilidadeTotal);
        carteira.setLucroTotal(lucroTotal);
        carteira.setRetornoDiariototal(retornoDiarioTotal);
        carteira.setStocksComRentabilidade(stocksComRentabilidade);


        return carteira;
    }

    private AnaliseTecnicaDTO createAnaliseTecnicaDTO(String chave, StockData data, AnaliseTecnicaService analise) {
        AnaliseTecnicaDTO dto = new AnaliseTecnicaDTO();
        dto.setSymbol(chave);
        dto.setCurrentPrice(data.getCurrentPrice());
        dto.setSma(analise.calcularSMA(14));
        dto.setRsi(analise.calcularRSI(14));
        dto.setBollinger(analise.calcularBandasBollinger(14, 2));
        dto.setRetornoDiario(analise.calcularRetornoDiario());
        dto.setVolumeMedio(analise.calcularVolumeMedio(14));
        return dto;
    }

    @GetMapping("/price")
    public String testPrice(@RequestParam String symbol) throws Exception {
        AnaliseTecnicaDTO dto = new AnaliseTecnicaDTO();
        String result = client.getPrice(symbol);
        StockData data = StockDataParser.parseJson(result);
        AnaliseTecnicaService analise = new AnaliseTecnicaService(data);
        dto.setSymbol(data.getSymbol());
        dto.setCurrentPrice(data.getCurrentPrice());
        dto.setSma(analise.calcularSMA(14));
        dto.setRsi(analise.calcularRSI(14));
        dto.setBollinger(analise.calcularBandasBollinger(14,2));
        dto.setRetornoDiario(analise.calcularRetornoDiario());
        dto.setVolumeMedio(analise.calcularVolumeMedio(14));

       // restTemplate.postForEntity(controleFinanceiroUrl+"/api/analise-tecnica",dto,Void.class);

       // return "redirect:"+controleFinanceiroUrl+"/home?symbol="+symbol;

        return dto.toString();
    }
}
