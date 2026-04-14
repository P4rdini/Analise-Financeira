package com.example.WalletProcessor.Service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ServiceRegistry {
    private final Map<String,ServiceStatus> serviceStatusMap = new ConcurrentHashMap<>();

    public ServiceRegistry(){
        serviceStatusMap.put("walletProcessor",ServiceStatus.ATIVO);
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
    public ResponseEntity<Map<String, String>> checkServiceStatus(String service) {
        Map<String, String> response = new HashMap<>();

        if (getStatus(service) != ServiceStatus.ATIVO) {
            response.put("status", "error");
            response.put("message", "Serviço " + service + " está inativo ou falhou.");
            return ResponseEntity.status(503).body(response);
        }

        response.put("status", "success");
        response.put("message", service + " está ativo.");
        return ResponseEntity.ok(response);
    }
}
