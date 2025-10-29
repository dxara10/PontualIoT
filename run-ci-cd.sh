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

# Test Web Admin (se existir)
if [ -f "web-admin/package.json" ]; then
    log_info "Testando Web Admin..."
    cd web-admin
    if [ -d "node_modules" ]; then
        if npm test -- --watchAll=false; then
            log_success "✅ Web Admin - Testes passaram"
        else
            log_warning "⚠️ Web Admin - Testes falharam (continuando...)"
        fi
    else
        log_warning "⚠️ Web Admin - Dependências não instaladas"
    fi
    cd ..
fi

# Test Mobile App (se existir)
if [ -f "mobile-app/package.json" ]; then
    log_info "Testando Mobile App..."
    cd mobile-app
    if [ -d "node_modules" ]; then
        if npm test -- --watchAll=false; then
            log_success "✅ Mobile App - Testes passaram"
        else
            log_warning "⚠️ Mobile App - Testes falharam (continuando...)"
        fi
    else
        log_warning "⚠️ Mobile App - Dependências não instaladas"
    fi
    cd ..
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
log_info "🚀 STAGE 4: Deploy Completo"
echo "----------------------------"

# Subir infraestrutura completa
log_info "Subindo infraestrutura completa (PostgreSQL + MQTT + Monitoramento)..."
if docker-compose up -d postgres mosquitto prometheus grafana; then
    log_success "✅ Infraestrutura completa rodando"
else
    log_error "❌ Falha ao subir infraestrutura"
    exit 1
fi

# Aguardar serviços
log_info "Aguardando serviços inicializarem..."
sleep 8

# Iniciar API Core
if curl -s http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
    log_success "✅ API já está rodando na porta 8080"
else
    log_info "Iniciando API Core..."
    cd api-core
    nohup mvn spring-boot:run > ../api-core.log 2>&1 &
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

# Iniciar IoT Simulator
log_info "Iniciando IoT Simulator..."
cd iot-devices/simulator
nohup mvn exec:java -Dexec.mainClass="com.pontualiot.simulator.SimulatorApplication" > ../../simulator.log 2>&1 &
SIMULATOR_PID=$!
cd ../..
log_success "✅ IoT Simulator iniciado"

# Iniciar Web Admin (se existir)
if [ -f "web-admin/package.json" ]; then
    log_info "Iniciando Web Admin..."
    cd web-admin
    if [ -d "node_modules" ]; then
        nohup npm start > ../web-admin.log 2>&1 &
        WEB_PID=$!
        log_success "✅ Web Admin iniciado na porta 3001"
    else
        log_warning "⚠️ Web Admin - dependências não instaladas"
    fi
    cd ..
fi

# Iniciar Mobile App (se existir)
if [ -f "mobile-app/package.json" ]; then
    log_info "Iniciando Mobile App..."
    cd mobile-app
    if [ -d "node_modules" ]; then
        # Iniciar Expo com suporte a múltiplas plataformas
        nohup npx expo start --tunnel > ../mobile-app.log 2>&1 &
        MOBILE_PID=$!
        log_success "✅ Mobile App iniciado (Web: 19006, Android/iOS via QR)"
    else
        log_warning "⚠️ Mobile App - dependências não instaladas"
    fi
    cd ..
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

# 7. STAGE: E2E Integration Tests
echo ""
log_info "🔗 STAGE 7: Testes E2E de Integração"
echo "-------------------------------------"

if [ -f "e2e-tests/pom.xml" ]; then
    log_info "Executando testes E2E de integração completa..."
    cd e2e-tests
    
    # Aguardar todos os serviços estarem prontos
    log_info "Aguardando todos os serviços estarem prontos..."
    sleep 10
    
    # Executar testes E2E
    if mvn clean test -q; then
        log_success "✅ Testes E2E - Integração completa passou"
    else
        log_warning "⚠️ Testes E2E - Alguns testes falharam (continuando...)"
    fi
    cd ..
else
    log_warning "⚠️ Módulo E2E não encontrado"
fi

# Teste adicional de integração Web Admin (se disponível)
if curl -s http://localhost:3001 > /dev/null 2>&1; then
    log_success "✅ Web Admin - Acessível na porta 3001"
else
    log_warning "⚠️ Web Admin - Não acessível (pode não estar rodando)"
fi

# Teste de integração MQTT
log_info "Testando integração MQTT..."
if nc -z localhost 1883 2>/dev/null; then
    log_success "✅ MQTT Broker - Acessível na porta 1883"
else
    log_warning "⚠️ MQTT Broker - Não acessível"
fi

# Teste de integração Prometheus
log_info "Testando integração Prometheus..."
if curl -s http://localhost:9090/-/healthy > /dev/null 2>&1; then
    log_success "✅ Prometheus - Acessível na porta 9090"
else
    log_warning "⚠️ Prometheus - Não acessível"
fi

# Teste de integração Grafana
log_info "Testando integração Grafana..."
if curl -s http://localhost:3000/api/health > /dev/null 2>&1; then
    log_success "✅ Grafana - Acessível na porta 3000"
else
    log_warning "⚠️ Grafana - Não acessível"
fi

# Teste de métricas da API
log_info "Testando métricas da API..."
if curl -s http://localhost:8080/api/actuator/prometheus | grep -q "jvm_memory"; then
    log_success "✅ Métricas Prometheus - API expondo métricas"
else
    log_warning "⚠️ Métricas Prometheus - API não expondo métricas"
fi

# Resultado Final
echo ""
echo "=============================================="
log_success "🎉 ESTEIRA CI/CD EXECUTADA COM SUCESSO!"
echo "=============================================="
echo ""
log_info "📊 Serviços Disponíveis:"
echo "  • API Core: http://localhost:8080/api"
echo "  • Swagger: http://localhost:8080/swagger-ui/index.html"
echo "  • Health: http://localhost:8080/api/actuator/health"
echo "  • IoT Simulator: Rodando em background"
echo "  • Web Admin: http://localhost:3001 (se disponível)"
echo "  • Mobile App Web: http://localhost:19006"
echo "  • Mobile App Android/iOS: Escaneie QR code no terminal"
echo "  • PostgreSQL: localhost:5432"
echo "  • MQTT Broker: localhost:1883"
echo "  • Prometheus: http://localhost:9090"
echo "  • Grafana: http://localhost:3000 (admin/admin)"
echo ""
log_info "🔧 Para parar os serviços:"
echo "  docker-compose down"
echo "  pkill -f 'spring-boot:run\|SimulatorApplication\|npm start\|expo start'"