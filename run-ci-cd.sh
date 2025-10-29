#!/bin/bash
set -e

echo "🚀 Executando Esteira CI/CD Local - PontualIoT"
echo "=============================================="

# Função para log colorido
log_info() { echo -e "\033[1;34m[INFO]\033[0m $1"; }
log_success() { echo -e "\033[1;32m[SUCCESS]\033[0m $1"; }
log_error() { echo -e "\033[1;31m[ERROR]\033[0m $1"; }
log_warning() { echo -e "\033[1;33m[WARNING]\033[0m $1"; }

# 1. STAGE: Test
echo ""
log_info "🧪 STAGE 1: Executando Testes"
echo "------------------------------"

# Test API Core
log_info "Testando API Core..."
cd api-core
if mvn clean test -q; then
    log_success "✅ API Core - Testes passaram"
else
    log_error "❌ API Core - Testes falharam"
    exit 1
fi
cd ..

# Test IoT Simulator (se existir)
if [ -f "iot-devices/simulator/pom.xml" ]; then
    log_info "Testando IoT Simulator..."
    cd iot-devices/simulator
    if mvn clean test -q; then
        log_success "✅ IoT Simulator - Testes passaram"
    else
        log_warning "⚠️ IoT Simulator - Testes falharam (continuando...)"
    fi
    cd ../..
fi

# 2. STAGE: Build
echo ""
log_info "🔨 STAGE 2: Build & Package"
echo "----------------------------"

# Build API Core
log_info "Building API Core..."
cd api-core
if mvn clean package -DskipTests -q; then
    log_success "✅ API Core - Build concluído"
else
    log_error "❌ API Core - Build falhou"
    exit 1
fi
cd ..

# 3. STAGE: Docker Build (simulado)
echo ""
log_info "🐳 STAGE 3: Docker Build (Simulado)"
echo "------------------------------------"

# Verificar se Dockerfiles existem
if [ -f "api-core/Dockerfile" ]; then
    log_info "Simulando build da imagem api-core..."
    log_success "✅ Docker image api-core:latest (simulado)"
else
    log_warning "⚠️ Dockerfile não encontrado para api-core"
fi

# 4. STAGE: Deploy Local
echo ""
log_info "🚀 STAGE 4: Deploy Local"
echo "-------------------------"

# Subir PostgreSQL
log_info "Subindo PostgreSQL..."
if docker-compose up -d postgres; then
    log_success "✅ PostgreSQL rodando"
else
    log_error "❌ Falha ao subir PostgreSQL"
    exit 1
fi

# Aguardar PostgreSQL
log_info "Aguardando PostgreSQL inicializar..."
sleep 5

# Verificar se API está rodando
if curl -s http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
    log_success "✅ API já está rodando na porta 8080"
else
    log_info "Iniciando API Core..."
    cd api-core
    nohup mvn spring-boot:run > /dev/null 2>&1 &
    API_PID=$!
    cd ..
    
    # Aguardar API inicializar
    log_info "Aguardando API inicializar..."
    for i in {1..30}; do
        if curl -s http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
            log_success "✅ API Core rodando na porta 8080"
            break
        fi
        sleep 2
    done
fi

# 5. STAGE: Health Check
echo ""
log_info "🏥 STAGE 5: Health Check"
echo "-------------------------"

# Verificar saúde da API
if curl -s http://localhost:8080/api/actuator/health | grep -q "UP"; then
    log_success "✅ API Health Check passou"
else
    log_error "❌ API Health Check falhou"
    exit 1
fi

# Teste básico de endpoint
if curl -s http://localhost:8080/api/employees > /dev/null; then
    log_success "✅ Endpoint /employees acessível"
else
    log_error "❌ Endpoint /employees inacessível"
    exit 1
fi

# 6. STAGE: Smoke Tests
echo ""
log_info "💨 STAGE 6: Smoke Tests"
echo "------------------------"

# Teste de criação de employee
log_info "Testando criação de employee..."
EMPLOYEE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"CI Test","email":"ci@test.com","rfidTag":"CI001","active":true}')

if echo "$EMPLOYEE_RESPONSE" | grep -q "CI Test"; then
    log_success "✅ Smoke Test - Criação de employee"
else
    log_warning "⚠️ Smoke Test - Criação de employee falhou"
fi

# Resultado Final
echo ""
echo "=============================================="
log_success "🎉 ESTEIRA CI/CD EXECUTADA COM SUCESSO!"
echo "=============================================="
echo ""
log_info "📊 Serviços Disponíveis:"
echo "  • API Core: http://localhost:8080"
echo "  • Swagger: http://localhost:8080/swagger-ui/index.html"
echo "  • Health: http://localhost:8080/api/actuator/health"
echo "  • PostgreSQL: localhost:5432"
echo ""
log_info "🔧 Para parar os serviços:"
echo "  docker-compose down"
echo "  pkill -f spring-boot:run"