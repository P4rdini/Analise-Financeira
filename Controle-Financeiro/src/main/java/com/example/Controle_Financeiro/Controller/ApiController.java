package com.example.Controle_Financeiro.Controller;

import com.example.Controle_Financeiro.DTO.*;
import com.example.Controle_Financeiro.JWT.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private AuthController authController;
    @Autowired 
    private RestTemplate restTemplate;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request){
        try{
            System.out.println("tentou logar");
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword()));
            String token = jwtUtil.generateToken(request.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/home")
    public ResponseEntity<?> home(Principal principal){
        String username = principal.getName();
        try{
            List<StockDTO> stocks = authController.getAçõesCarteira(username);
            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("stocks",stocks);

            return ResponseEntity.ok(response);
        }catch (Exception e){
            String servidor = "";
            if(e.getMessage().contains("8081")){
                servidor+="Stock Fetcher ";
            }else if (e.getMessage().contains("8082")){
                servidor += "Wallet Processor ";
            }else{
                servidor += "Wallet Storage";
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Serviço "+servidor+" indisponivel. Tente novamente mais tarde");
        }
    }

    @GetMapping("/wallet")
    public ResponseEntity<?> getCarteira(Principal principal){
        String username = principal.getName();
        try{
            SequencedCollection<StockDTO> acoes = authController.getAçõesCarteira(username);
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
                return ResponseEntity.ok(carteira);
            }else{
                return ResponseEntity.ok(getFallback());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao buscar carteira: " + e.getMessage());
        }
    }

    @GetMapping("/transacao")
    public ResponseEntity<?> getTransacao(Principal principal){
        String username = principal.getName();
        try{
            ResponseEntity<TransactionDTO[]> result = restTemplate.getForEntity("http://localhost:8083/wallets/" + principal.getName() + "/transactions", TransactionDTO[].class);
            return ResponseEntity.ok(result.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falha ao processar lançamento. Serviço temporariamente indisponível."+e.getMessage());
        }
    }
    @PostMapping("/lancamentos/adicionar")
    public ResponseEntity<?> adicionarLancamento(@RequestBody LancamentoDTO lancamento,Principal principal){
        try{
            String urlBase = "http://localhost:8083/wallets/" + principal.getName();
            if ("COMPRA".equalsIgnoreCase(lancamento.getOperacao())) {
                restTemplate.postForEntity(
                        urlBase + "/buy?symbol=" + lancamento.getAcao() +
                                "&quantity=" + lancamento.getQuantidade() +
                                "&price=" + lancamento.getValor(),
                        null,
                        Void.class
                );
            } else {
                restTemplate.postForEntity(
                        urlBase + "/sell?symbol=" + lancamento.getAcao() +
                                "&quantity=" + lancamento.getQuantidade() +
                                "&price=" + lancamento.getValor(),
                        null,
                        Void.class
                );
            }

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falha ao processar lançamento. Serviço temporariamente indisponível.");
        }
    }

    public CarteiraDTO getFallback(){
        CarteiraDTO fallback = new CarteiraDTO();
        fallback.setRentabilidadeTotal(0.0);
        fallback.setTotalAtual(0.0);
        fallback.setTotalInvestido(0.0);
        fallback.setStocksComRentabilidade(Collections.emptyList());
        return fallback;
    }
}
