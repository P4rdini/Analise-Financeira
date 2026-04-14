package com.example.Controle_Financeiro.Controller;

import com.example.Controle_Financeiro.Client.StockFetcherClient;
import com.example.Controle_Financeiro.DTO.*;
import com.example.Controle_Financeiro.JWT.JwtUtil;
import com.example.Controle_Financeiro.Service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AuthController {

    @Autowired private StockFetcherClient stockFetcherClient;
    private String urlStorage = "http://localhost:8083/wallets/";


    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuthenticationManager authManager;
    @Autowired private RestTemplate restTemplate;


    @GetMapping("/transacao")
    public String getTransacoes(Principal principal,Model model){

        try{
            ResponseEntity<TransactionDTO[]> result = restTemplate.getForEntity("http://localhost:8083/wallets/" + principal.getName() + "/transactions", TransactionDTO[].class);
            model.addAttribute("transacoes",result.getBody());
        } catch (Exception e) {
            System.err.println("Falha ao processar lançamento. Serviço temporariamente indisponível."+e.getMessage());
            return "redirect:/home";
        }
        return "transacao";
    }
        @PostMapping("/lancamentos/adicionar")
    public String adicionarLancamento(@ModelAttribute LancamentoDTO lancamentoDTO, Principal principal) {


        try {
            if(lancamentoDTO.getOperacao().equals("COMPRA")){
                restTemplate.postForEntity("http://localhost:8083/wallets/"+principal.getName()+"/buy?symbol="+lancamentoDTO.getAcao()+"&quantity="+lancamentoDTO.getQuantidade()+"&price="+lancamentoDTO.getValor(),null,Void.class);
            }else{
                restTemplate.postForEntity("http://localhost:8083/wallets/"+principal.getName()+"/sell?symbol="+lancamentoDTO.getAcao()+"&quantity="+lancamentoDTO.getQuantidade()+"&price="+lancamentoDTO.getValor(),null,Void.class);
            }
        } catch (Exception e) {
            System.err.println("Falha ao processar lançamento. Serviço temporariamente indisponível.");
            return "redirect:/home";

        }
        return "redirect:/home";
    }


    @GetMapping("/register")
    public String showRegisterForm(Model model){
        model.addAttribute("authRequest",new AuthRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(AuthRequest request,Model model){
            userService.registrarUser(request.getUsername(),request.getPassword());

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model){
        model.addAttribute("authRequest",new AuthRequest());
        return "login";
    }

    @PostMapping("/do-login")
    public String login(AuthRequest request,HttpServletResponse response, Model model){
        try{
            Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            String token = jwtUtil.generateToken(request.getUsername());

            // Adiciona o token como cookie
            Cookie jwtCookie = new Cookie("JWT", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(86400); // 1 dia
            response.addCookie(jwtCookie);

            response.addHeader("Authorization","Bearer "+token);

            return "redirect:/home";
        }catch (Exception e){
            model.addAttribute("error","Usuario ou senha inválidos "+e.getMessage());
            return"login";
        }
    }
    @GetMapping("/do-logout")
    public String logout(HttpServletResponse response){
        Cookie cookie = new Cookie("JWT",null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return "redirect:/login";
    }

    @GetMapping("/wallet")
    public String getCarteira(Model model,Principal principal){
        String username = principal.getName();
        try {

            SequencedCollection<StockDTO> acoes = getAçõesCarteira(principal.getName());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SequencedCollection<StockDTO>> entity = new HttpEntity<>(acoes, headers);
            ResponseEntity<CarteiraDTO> response = restTemplate.postForEntity(
                    "http://localhost:8082/test/snapshot/"+ username +"/carteira",
                    entity,
                    CarteiraDTO.class
            );
            CarteiraDTO carteira = response.getBody();

            if(carteira != null){
                model.addAttribute("stocksComRentabilidade", carteira.getStocksComRentabilidade());
                model.addAttribute("totalInvestido", carteira.getTotalInvestido());
                model.addAttribute("totalAtual",carteira.getTotalAtual());
                model.addAttribute("rentabilidadeTotal", carteira.getRentabilidadeTotal());
            }else {
                adicionarFallback(model);
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar carteira: " + e.getMessage());
            adicionarFallback(model);
        }

        return "minhaCarteira";
    }

    private void adicionarFallback(Model model) {
        model.addAttribute("stocksComRentabilidade", Collections.emptyList());
        model.addAttribute("totalInvestido", 0.0);
        model.addAttribute("totalAtual", 0.0);
        model.addAttribute("rentabilidadeTotal", 0.0);
    }


    @GetMapping("/home")
    public String home(Model model, Principal principal){
        String username = principal.getName();
        model.addAttribute("username",username);

        try {
            SequencedCollection<StockDTO> stocks = getAçõesCarteira(username);
            System.out.println(stocks.toString());
            model.addAttribute("stocks",stocks);
        }catch ( Exception e){
            String servidor = "";
            if(e.getMessage().contains("8081")){
                servidor+="Stock Fetcher ";
            }else if (e.getMessage().contains("8082")){
                servidor += "Wallet Processor ";
            }else{
                servidor += "Wallet Storage";
            }
            model.addAttribute("errorMessage","Serviço "+servidor+" indisponivel. Tente novamente mais tarde");
            System.out.println("erro : " + e.getMessage());
            return "home";
        }
        return"home";
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
