package com.example.WalletProcessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class WalletProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(WalletProcessorApplication.class, args);
	}

}
