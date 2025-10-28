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
# 1. Subir PostgreSQL
docker-compose up -d postgres

# 2. Rodar API Core
cd api-core
mvn spring-boot:run

# 3. Verificar funcionamento
curl http://localhost:8082/actuator/health
```

## 📊 Acesso aos Serviços
- **API Core**: http://localhost:8082
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **Health Check**: http://localhost:8082/actuator/health
- **PostgreSQL**: localhost:5432 (postgres/postgres)

## 🧪 Testes
```bash
# Executar todos os testes
cd api-core && mvn test

# Teste específico
mvn test -Dtest=AttendanceTestControllerTDDTest
```