package com.example.StockFetcher.exception;

import lombok.Getter;

@Getter
public class ServiceUnavailableException extends RuntimeException{
    private String serviceName;

    public ServiceUnavailableException(String serviceName){
        super("Serviço "+serviceName+" esta inativo ou falhou");
        this.serviceName = serviceName;
    }

}
