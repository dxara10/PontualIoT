# API Core - Serviço Principal

## 🎯 Responsabilidades
- Gerenciamento de empresas, funcionários e dispositivos
- Processamento de eventos de ponto
- API REST com autenticação JWT
- Integração com MQTT e HTTP

## 🚀 Como Rodar o Projeto

### 1. Subir PostgreSQL
```bash
# Na raiz do projeto
docker-compose up -d postgres
```

### 2. Rodar a API
```bash
# Entrar na pasta api-core
cd api-core

# Rodar com Maven
mvn spring-boot:run
```

### 3. Verificar se está funcionando
```bash
# Health check
curl http://localhost:8082/api/actuator/health

# Swagger UI
http://localhost:8082/api/swagger-ui/index.html
```

## 🧪 Testes
```bash
# Todos os testes
mvn test

# Teste específico
mvn test -Dtest=AttendanceTestControllerTDDTest
```

## 📊 Endpoints Principais
- `GET /api/actuator/health` - Health check
- `GET /api/swagger-ui/index.html` - Documentação da API
- `POST /api/api/test-attendance/check-in/{employeeId}` - Teste check-in
- `POST /api/api/test-attendance/check-out/{employeeId}` - Teste check-out
- `GET /api/api/employees` - Listar funcionários
- `POST /api/api/employees` - Criar funcionário
- `GET /api/api/attendances` - Listar registros de ponto
- `GET /api/api/attendances/employee/{employeeId}` - Registros por funcionário

## 🔧 Configurações
- **Porta**: 8082 (definida no application.properties)
- **Context Path**: /api (definido no application.yml)
- **Database**: PostgreSQL (localhost:5432)

## 📝 Exemplo de Uso
```bash
# 1. Primeiro criar um funcionário
curl -X POST http://localhost:8082/api/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"João","email":"joao@test.com","rfidTag":"RFID001"}'

# 2. Fazer check-in (SEM headers, SEM body)
curl -X POST http://localhost:8082/api/api/test-attendance/check-in/1

# 3. Fazer check-out (SEM headers, SEM body)
curl -X POST http://localhost:8082/api/api/test-attendance/check-out/1
```

## ⚠️ Solução de Problemas
- **Erro 500**: Employee ID não existe - crie um funcionário primeiro
- **Porta**: API roda na 8082, não 8080
- **Database**: PostgreSQL deve estar rodando (docker-compose up -d postgres)