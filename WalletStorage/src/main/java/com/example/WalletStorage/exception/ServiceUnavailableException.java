package com.example.WalletStorage.exception;

public class ServiceUnavailableException extends RuntimeException {

    private String serviceName;

    public ServiceUnavailableException(String serviceName){
        super("Serviço "+serviceName+" esta inativo ou falhou");
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
