package com.example.Controle_Financeiro.Service;

import com.example.Controle_Financeiro.Model.User;
import com.example.Controle_Financeiro.Repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RabbitTemplate rabbitTemplate;
    @Autowired private RestTemplate restTemplate;
    @Value("${wallet-storage.url}") private String walletStorageUrl;


    @Transactional
    @Retryable(maxAttempts = 3,backoff = @Backoff(delay = 1000,multiplier = 2))
    public void registrarUser(String username, String password)  {
        if(userRepository.findByUsername(username).isPresent()){
            throw new RuntimeException("Usuario ja existe");
        }

        User user = new User(username,passwordEncoder.encode(password));
        userRepository.save(user);

        String url = walletStorageUrl + "/wallets/create?userId="+username;
        ResponseEntity<Void> response = restTemplate.postForEntity(url,null,Void.class);

        if(!response.getStatusCode().is2xxSuccessful()){
            throw new RuntimeException("Falha ao criar carteira");
        }
    }

}
