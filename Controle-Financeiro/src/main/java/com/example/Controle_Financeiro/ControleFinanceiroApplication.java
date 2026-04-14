package com.example.Controle_Financeiro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.example.Controle_Financeiro.Client")
@SpringBootApplication
public class ControleFinanceiroApplication {

	public static void main(String[] args) {
		SpringApplication.run(ControleFinanceiroApplication.class, args);
	}

}
