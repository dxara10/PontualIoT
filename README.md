# Smart Attendance Platform - Sistema IoT de Registro de Ponto

## 🎯 Visão Geral
Sistema de microsserviços para registro de ponto digital via dispositivos IoT, suportando MQTT, HTTP e SDK.

## 🏗️ Arquitetura
- **api-core**: Serviço principal (Spring Boot)
- **iot-devices**: Simuladores e drivers
- **web-admin**: Interface administrativa (React)
- **mobile-app**: App móvel (React Native)
- **infra**: Infraestrutura e CI/CD

## 🚀 Quick Start
```bash
# Opção 1: Sistema completo (CI/CD)
./run-ci-cd.sh

# Opção 2: Apenas módulos de negócio
./start-business.sh

# Opção 3: Apenas monitoramento
./start-monitoring.sh

# Verificar funcionamento
curl http://localhost:8080/api/actuator/health
```

## 📊 Acesso aos Serviços
- **API Core**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/api/actuator/health
- **Web Admin**: http://localhost:3001
- **Mobile App**: http://localhost:19006 (Web) + QR Code (Android/iOS)
- **PostgreSQL**: localhost:5432 (postgres/postgres)
- **MQTT Broker**: localhost:1883
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090

## 🧪 Testes
```bash
# Executar todos os testes unitários
cd api-core && mvn test

# Teste específico
mvn test -Dtest=AttendanceTestControllerTDDTest

# Executar esteira CI/CD completa
bash run-ci-cd.sh

# Executar apenas testes E2E de integração
bash run-e2e-integration.sh
```

### 🔗 Testes E2E de Integração
Os testes E2E verificam a integração completa entre todos os módulos:

- **Infraestrutura**: API Core + PostgreSQL + MQTT + Prometheus + Grafana
- **CRUD Completo**: Operações de Employee com validações
- **Fluxo de Attendance**: Check-in/Check-out via API e MQTT
- **Métricas**: Coleta e exposição de métricas customizadas
- **Performance**: Testes de responsividade e carga básica
- **Validações**: Regras de negócio (RFID único, email válido)
- **Integração MQTT**: Processamento de mensagens IoT
- **Monitoramento**: Health checks de todos os serviços

## 🚀 Scripts Disponíveis
```bash
# CI/CD completo (testes + deploy + monitoramento)
./run-ci-cd.sh

# Apenas módulos de negócio (API + IoT + Web)
./start-business.sh

# Apenas stack de monitoramento (Grafana + Prometheus)
./start-monitoring.sh

# Sistema completo com monitoramento
./run-complete-system.sh

# Testes E2E de integração específicos
./run-e2e-integration.sh
```

📚 **[Documentação Completa CI/CD](docs/ci-cd.md)**