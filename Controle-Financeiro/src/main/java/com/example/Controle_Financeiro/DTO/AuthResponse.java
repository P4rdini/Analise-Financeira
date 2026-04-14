package com.example.Controle_Financeiro.DTO;

public class AuthResponse {
    private String token;

    public AuthResponse(String token){
        this.token = token;
    }

    public String getResponse(){
        return token;
    }
}
