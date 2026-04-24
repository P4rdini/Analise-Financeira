package com.example.Controle_Financeiro.Controller;

import com.example.Controle_Financeiro.Client.StockFetcherClient;
import com.example.Controle_Financeiro.DTO.*;
import com.example.Controle_Financeiro.JWT.JwtUtil;
import com.example.Controle_Financeiro.Service.UserService;
import com.example.Controle_Financeiro.Service.WalletService;
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


    private String urlStorage = "http://localhost:8083/wallets/";


    @Autowired private UserService userService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuthenticationManager authManager;
    @Autowired private RestTemplate restTemplate;
    @Autowired private WalletService walletService;


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

            SequencedCollection<StockDTO> acoes = walletService.getAçõesCarteira(principal.getName());

            CarteiraDTO carteira = walletService.montarCarteira(username,acoes);

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
            SequencedCollection<StockDTO> stocks = walletService.getAçõesCarteira(username);
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


}
