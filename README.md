# PontualIoT - Smart Attendance Platform

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Coverage](https://img.shields.io/badge/coverage-95%25-brightgreen)]()
[![License](https://img.shields.io/badge/license-MIT-blue)]()
[![Java](https://img.shields.io/badge/Java-21-orange)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-green)]()
[![React](https://img.shields.io/badge/React-18.2.0-blue)]()
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-blue)]()

## 📋 Índice
- [Visão Geral](#-visão-geral)
- [Arquitetura](#️-arquitetura)
- [Tecnologias](#-tecnologias)
- [Instalação](#-instalação)
- [Uso](#-uso)
- [API Documentation](#-api-documentation)
- [Testes](#-testes)
- [Monitoramento](#-monitoramento)
- [Deployment](#-deployment)
- [Contribuição](#-contribuição)

## 🎯 Visão Geral

O **PontualIoT** é uma plataforma completa de controle de ponto digital baseada em IoT, desenvolvida com arquitetura de microsserviços. O sistema oferece registro de ponto via dispositivos RFID, interface web administrativa, aplicativo móvel e integração MQTT para dispositivos IoT.

### ✨ Principais Funcionalidades

- 🏷️ **Registro via RFID**: Controle de ponto através de tags RFID
- 📱 **App Móvel**: Interface React Native para funcionários
- 🖥️ **Painel Admin**: Dashboard web para gestores
- 📡 **Integração MQTT**: Comunicação em tempo real com dispositivos IoT
- 📊 **Relatórios**: Análise de dados de frequência e produtividade
- 🔒 **Segurança**: Autenticação JWT e validações robustas
- 📈 **Monitoramento**: Métricas Prometheus + Grafana
- 🧪 **Testes**: Cobertura completa com testes unitários e E2E

## 🏗️ Arquitetura

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Mobile App    │    │    Web Admin    │    │  IoT Devices    │
│  (React Native) │    │     (React)     │    │   (Simulators)  │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────┴───────────┐
                    │      API Gateway        │
                    │    (Spring Boot)        │
                    └─────────────┬───────────┘
                                  │
              ┌───────────────────┼───────────────────┐
              │                   │                   │
    ┌─────────┴───────┐ ┌─────────┴───────┐ ┌─────────┴───────┐
    │   PostgreSQL    │ │   MQTT Broker   │ │   Monitoring    │
    │   (Database)    │ │   (Eclipse)     │ │ (Prometheus +   │
    │                 │ │                 │ │   Grafana)      │
    └─────────────────┘ └─────────────────┘ └─────────────────┘
```

### 📦 Módulos

| Módulo | Tecnologia | Descrição | Porta |
|--------|------------|-----------|-------|
| **api-core** | Spring Boot 3.2.1 | API REST principal com lógica de negócio | 8080 |
| **web-admin** | React 18.2.0 | Interface administrativa para gestores | 3001 |
| **mobile-app** | React Native | Aplicativo móvel para funcionários | 19006 |
| **iot-devices** | Python/Node.js | Simuladores de dispositivos IoT | - |
| **infra** | Docker Compose | Infraestrutura e orquestração | - |

## 🛠 Tecnologias

### Backend
- **Java 21** - Linguagem principal
- **Spring Boot 3.2.1** - Framework web
- **Spring Data JPA** - Persistência de dados
- **Spring Security** - Autenticação e autorização
- **PostgreSQL 14** - Banco de dados principal
- **H2** - Banco em memória para testes
- **MQTT** - Protocolo IoT
- **JWT** - Tokens de autenticação

### Frontend
- **React 18.2.0** - Interface web
- **React Native** - Aplicativo móvel
- **Axios** - Cliente HTTP
- **Material-UI** - Componentes UI

### DevOps & Monitoramento
- **Docker & Docker Compose** - Containerização
- **Prometheus** - Coleta de métricas
- **Grafana** - Visualização de dados
- **Maven** - Gerenciamento de dependências
- **JUnit 5** - Testes unitários

## 🚀 Instalação

### Pré-requisitos

- **Java 21+**
- **Node.js 18+**
- **Docker & Docker Compose**
- **Maven 3.8+**
- **Git**

### Instalação Rápida

```bash
# 1. Clonar o repositório
git clone https://github.com/seu-usuario/pontualiot.git
cd pontualiot

# 2. Executar sistema completo
./run-ci-cd.sh

# 3. Verificar funcionamento
curl http://localhost:8080/api/actuator/health
```

### Instalação Manual

```bash
# 1. Subir infraestrutura
docker-compose -f infra/docker-compose.yml up -d

# 2. Compilar e executar API
cd api-core
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 3. Executar Web Admin
cd ../web-admin
npm install
npm start

# 4. Executar Mobile App
cd ../mobile-app
npm install
npm start
```

## 💻 Uso

### Acesso aos Serviços

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| **API REST** | http://localhost:8080/api | - |
| **Swagger UI** | http://localhost:8080/swagger-ui/index.html | - |
| **Web Admin** | http://localhost:3001 | admin/admin |
| **Mobile App** | http://localhost:19006 | - |
| **Grafana** | http://localhost:3000 | admin/admin |
| **Prometheus** | http://localhost:9090 | - |
| **PostgreSQL** | localhost:5432 | postgres/postgres |
| **MQTT Broker** | localhost:1883 | - |

### Exemplos de Uso da API

```bash
# Listar funcionários
curl -X GET http://localhost:8080/api/employees

# Criar funcionário
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@empresa.com",
    "rfidTag": "RFID001",
    "active": true
  }'

# Consultar registros de ponto
curl -X GET http://localhost:8080/api/attendances/employee/1

# Health check
curl -X GET http://localhost:8080/api/actuator/health
```

## 📚 API Documentation

### Endpoints Principais

#### Employees
- `GET /api/employees` - Listar funcionários
- `POST /api/employees` - Criar funcionário
- `GET /api/employees/{id}` - Buscar por ID
- `PUT /api/employees/{id}` - Atualizar funcionário
- `DELETE /api/employees/{id}` - Remover funcionário

#### Attendances
- `GET /api/attendances` - Listar registros
- `GET /api/attendances/employee/{id}` - Registros por funcionário
- `GET /api/attendances/date/{date}` - Registros por data

#### Reports
- `GET /api/reports/attendance` - Relatório de frequência
- `GET /api/reports/summary` - Resumo executivo

### Documentação Completa
Acesse a documentação interativa em: http://localhost:8080/swagger-ui/index.html

## 🧪 Testes

### Executar Testes

```bash
# Todos os testes unitários
cd api-core && mvn test

# Teste específico
mvn test -Dtest=AttendanceTestControllerTDDTest

# Testes E2E de integração
bash run-e2e-integration.sh

# Esteira CI/CD completa
bash run-ci-cd.sh
```

### Cobertura de Testes

- ✅ **39 testes unitários** - 100% de sucesso
- ✅ **Testes de integração** - API + Database + MQTT
- ✅ **Testes E2E** - Fluxo completo do usuário
- ✅ **Testes de performance** - Responsividade < 100ms
- ✅ **Validações de negócio** - RFID único, email válido

### Tipos de Teste

| Tipo | Descrição | Localização |
|------|-----------|-------------|
| **Unitários** | Testes de componentes isolados | `src/test/java` |
| **Integração** | Testes de módulos integrados | `src/test/java/tdd` |
| **E2E** | Testes de fluxo completo | `run-e2e-integration.sh` |
| **Performance** | Testes de carga e responsividade | Incluído no E2E |

## 📊 Monitoramento

### Métricas Disponíveis

- 📈 **JVM Metrics** - Memória, CPU, threads
- 📊 **Application Metrics** - Requests, responses, errors
- 🏷️ **Custom Metrics** - Attendance records, employee operations
- 🔍 **Health Checks** - Database, MQTT, external services

### Dashboards Grafana

1. **System Overview** - Visão geral do sistema
2. **API Performance** - Performance da API REST
3. **Database Metrics** - Métricas do PostgreSQL
4. **MQTT Activity** - Atividade dos dispositivos IoT
5. **Business Metrics** - KPIs de negócio

### Alertas

- 🚨 **High Response Time** - Tempo de resposta > 1s
- 🚨 **Database Connection** - Falha na conexão
- 🚨 **MQTT Disconnection** - Dispositivos offline
- 🚨 **Memory Usage** - Uso de memória > 80%

## 🚀 Deployment

### Scripts de Deploy

```bash
# Deploy completo com CI/CD
./run-ci-cd.sh

# Deploy apenas módulos de negócio
./start-business.sh

# Deploy apenas monitoramento
./start-monitoring.sh

# Sistema completo
./run-complete-system.sh
```

### Ambientes

| Ambiente | Descrição | Profile |
|----------|-----------|----------|
| **Local** | Desenvolvimento local | `local` |
| **Test** | Ambiente de testes | `test` |
| **Staging** | Homologação | `staging` |
| **Production** | Produção | `prod` |

### Configuração por Ambiente

```yaml
# application-local.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pontualiot
  profiles:
    active: local

# application-prod.yml
spring:
  datasource:
    url: ${DATABASE_URL}
  profiles:
    active: prod
```

## 🤝 Contribuição

### Como Contribuir

1. **Fork** o projeto
2. **Crie** uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. **Commit** suas mudanças (`git commit -am 'Adiciona nova funcionalidade'`)
4. **Push** para a branch (`git push origin feature/nova-funcionalidade`)
5. **Abra** um Pull Request

### Padrões de Código

- **Java**: Seguir Google Java Style Guide
- **JavaScript**: ESLint + Prettier
- **Commits**: Conventional Commits
- **Testes**: Mínimo 80% de cobertura

### Estrutura de Commits

```
feat: adiciona nova funcionalidade
fix: corrige bug específico
docs: atualiza documentação
test: adiciona ou modifica testes
refactor: refatora código existente
```

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 📞 Suporte

- **Email**: suporte@pontualiot.com
- **Issues**: [GitHub Issues](https://github.com/seu-usuario/pontualiot/issues)
- **Wiki**: [Documentação Técnica](https://github.com/seu-usuario/pontualiot/wiki)

---

**Desenvolvido com ❤️ pela equipe PontualIoT**