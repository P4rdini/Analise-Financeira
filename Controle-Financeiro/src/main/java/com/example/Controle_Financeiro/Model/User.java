package com.example.Controle_Financeiro.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(){
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }


}
