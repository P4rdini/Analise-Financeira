package com.example.WalletStorage.Service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ServiceRegistry {
    private final Map<String,ServiceStatus> serviceStatusMap = new ConcurrentHashMap<>();

    public ServiceRegistry(){
        serviceStatusMap.put("walletStorage",ServiceStatus.ATIVO);
        serviceStatusMap.put("cache",ServiceStatus.ATIVO);
    }

    public Map<String,ServiceStatus> getAllStatuses(){
        return serviceStatusMap;
    }

    public void setServiceStatus(String service, ServiceStatus status) {
        serviceStatusMap.put(service, status);
    }

    public ServiceStatus getStatus(String service) {
        return serviceStatusMap.getOrDefault(service, ServiceStatus.FALHA);
    }
}
