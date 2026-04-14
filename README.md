# 💰 Sistema de Controle Financeiro (Microservices)

Aplicação de controle financeiro desenvolvida com arquitetura de microsserviços, focada em gerenciamento de carteira de investimentos, processamento de transações e integração com dados de mercado em tempo real.

---

## 🧠 Visão Geral

O sistema é composto por múltiplos serviços independentes, cada um com uma responsabilidade bem definida:

- 🔐 Autenticação e interface com usuário
- 📊 Processamento de regras de negócio
- 💾 Persistência de dados
- 📡 Coleta de dados de mercado (ações)

Essa abordagem segue princípios de **separação de responsabilidades**, **baixo acoplamento** e **escalabilidade**.

---

## 🏗️ Arquitetura
  - Frontend (Thymeleaf)

  - Controle-Financeiro (Gateway + Auth)
  - WalletProcessor
  - StockFetcher
  - WalletStorage


---

## 🔧 Tecnologias Utilizadas

### Backend
- Java
- Spring Boot
- Spring Security
- JWT (JSON Web Token)
- Maven

### Arquitetura
- Microsserviços
- REST APIs
- DTO Pattern

### Integrações
- API externa de mercado financeiro (Yahoo Finance)

### Frontend
- Thymeleaf
- Bootstrap

---

## 📦 Serviços

### 🔹 Controle-Financeiro
- Responsável pela autenticação de usuários
- Interface com o frontend
- Orquestra chamadas entre serviços
- Implementa segurança com JWT

---

### 🔹 WalletProcessor
- Contém as regras de negócio
- Processa transações
- Realiza cálculos financeiros da carteira

---

### 🔹 WalletStorage
- Responsável pela persistência dos dados
- Gerencia carteiras, ativos e transações

---

### 🔹 StockFetcher
- Consome dados de mercado externo
- Implementa cache
- Possui fallback para resiliência

---

## 🔐 Segurança

O sistema utiliza autenticação baseada em **JWT**, garantindo:

- Stateless authentication
- Proteção de rotas
- Controle de acesso por usuário

---

## 🔄 Fluxo de Funcionamento

1. Usuário realiza login
2. Recebe um token JWT
3. Realiza operações (compra/venda)
4. Requisição vai para o WalletProcessor
5. Dados são processados
6. Persistidos no WalletStorage
7. Dados de mercado são obtidos via StockFetcher

---

## 🚀 Funcionalidades

- Cadastro e autenticação de usuários
- Gerenciamento de carteira
- Registro de transações (compra/venda)
- Integração com dados de ações
- Processamento de regras financeiras
- Cache de dados externos

---

## 📌 Melhorias Futuras

- Implementação de mensageria (RabbitMQ/Kafka)
- Uso de Resilience4j (retry, circuit breaker)
- Testes automatizados (unitários e integração)
- Containerização com Docker
- API Gateway (Spring Cloud Gateway)
- Frontend com React

---

## 🧑‍💻 Autor

**Rafael Pardini**

- 💼 Desenvolvedor Java
- 🎓 Ciência da Computação - UNIP
- 🔗 GitHub: https://github.com/P4rdini

---

## 📈 Objetivo do Projeto

Este projeto foi desenvolvido com o objetivo de:

- Praticar arquitetura de microsserviços
- Aplicar conceitos de sistemas distribuídos
- Simular um sistema financeiro real
- Evoluir habilidades em backend com Java e Spring
