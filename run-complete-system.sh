#!/bin/bash
set -e

echo "🚀 Iniciando Sistema Completo - PontualIoT"
echo "=========================================="

# Função para log colorido
log_info() { echo -e "\033[1;34m[INFO]\033[0m $1"; }
log_success() { echo -e "\033[1;32m[SUCCESS]\033[0m $1"; }
log_error() { echo -e "\033[1;31m[ERROR]\033[0m $1"; }
log_warning() { echo -e "\033[1;33m[WARNING]\033[0m $1"; }

# 1. Subir infraestrutura completa
echo ""
log_info "🏗️ Subindo Infraestrutura Completa"
echo "-----------------------------------"

log_info "Iniciando PostgreSQL, MQTT, Prometheus e Grafana..."
if docker-compose up -d postgres mosquitto prometheus grafana; then
    log_success "✅ Infraestrutura completa iniciada"
else
    log_error "❌ Falha ao iniciar infraestrutura"
    exit 1
fi

# Aguardar serviços inicializarem
log_info "Aguardando serviços inicializarem..."
sleep 10

# 2. Verificar infraestrutura
echo ""
log_info "🔍 Verificando Infraestrutura"
echo "-----------------------------"

# PostgreSQL
if nc -z localhost 5432 2>/dev/null; then
    log_success "✅ PostgreSQL - Porta 5432"
else
    log_error "❌ PostgreSQL não está acessível"
    exit 1
fi

# MQTT
if nc -z localhost 1883 2>/dev/null; then
    log_success "✅ MQTT Broker - Porta 1883"
else
    log_error "❌ MQTT Broker não está acessível"
    exit 1
fi

# Prometheus
if nc -z localhost 9090 2>/dev/null; then
    log_success "✅ Prometheus - Porta 9090"
else
    log_warning "⚠️ Prometheus não está acessível"
fi

# Grafana
if nc -z localhost 3000 2>/dev/null; then
    log_success "✅ Grafana - Porta 3000"
else
    log_warning "⚠️ Grafana não está acessível"
fi

# 3. Iniciar API Core
echo ""
log_info "🔧 Iniciando API Core"
echo "--------------------"

# Verificar se já está rodando
if curl -s http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
    log_success "✅ API Core já está rodando"
else
    log_info "Compilando e iniciando API Core..."
    cd api-core
    
    # Build rápido
    if mvn clean package -DskipTests -q; then
        log_success "✅ API Core compilada"
    else
        log_error "❌ Falha na compilação da API Core"
        exit 1
    fi
    
    # Iniciar em background
    nohup mvn spring-boot:run > ../api-core.log 2>&1 &
    API_PID=$!
    cd ..
    
    # Aguardar inicialização
    log_info "Aguardando API Core inicializar..."
    for i in {1..30}; do
        if curl -s http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
            log_success "✅ API Core rodando na porta 8080"
            break
        fi
        sleep 2
        if [ $i -eq 30 ]; then
            log_error "❌ Timeout na inicialização da API Core"
            exit 1
        fi
    done
fi

# 4. Iniciar IoT Simulator
echo ""
log_info "📡 Iniciando IoT Simulator"
echo "--------------------------"

if [ -f "iot-devices/simulator/pom.xml" ]; then
    cd iot-devices/simulator
    log_info "Compilando IoT Simulator..."
    if mvn clean package -DskipTests -q; then
        log_success "✅ IoT Simulator compilado"
        
        # Iniciar em background
        nohup mvn exec:java -Dexec.mainClass="com.pontualiot.simulator.SimulatorApplication" > ../../simulator.log 2>&1 &
        SIMULATOR_PID=$!
        log_success "✅ IoT Simulator iniciado (PID: $SIMULATOR_PID)"
    else
        log_warning "⚠️ Falha na compilação do IoT Simulator"
    fi
    cd ../..
else
    log_warning "⚠️ IoT Simulator não encontrado"
fi

# 5. Iniciar Web Admin
echo ""
log_info "🌐 Iniciando Web Admin"
echo "----------------------"

if [ -f "web-admin/package.json" ]; then
    cd web-admin
    
    # Verificar dependências
    if [ ! -d "node_modules" ]; then
        log_info "Instalando dependências do Web Admin..."
        if npm install --silent; then
            log_success "✅ Dependências instaladas"
        else
            log_warning "⚠️ Falha na instalação de dependências"
            cd ..
            return
        fi
    fi
    
    # Iniciar em background
    log_info "Iniciando Web Admin..."
    nohup npm start > ../web-admin.log 2>&1 &
    WEB_PID=$!
    
    # Aguardar inicialização
    sleep 5
    if curl -s http://localhost:3001 > /dev/null 2>&1; then
        log_success "✅ Web Admin rodando na porta 3001"
    else
        log_warning "⚠️ Web Admin pode estar inicializando..."
    fi
    cd ..
else
    log_warning "⚠️ Web Admin não encontrado"
fi

# 6. Iniciar Mobile App
echo ""
log_info "📱 Iniciando Mobile App"
echo "-----------------------"

if [ -f "mobile-app/package.json" ]; then
    cd mobile-app
    
    # Verificar dependências
    if [ ! -d "node_modules" ]; then
        log_info "Instalando dependências do Mobile App..."
        if npm install --silent; then
            log_success "✅ Dependências instaladas"
        else
            log_warning "⚠️ Falha na instalação de dependências"
            cd ..
            return
        fi
    fi
    
    # Iniciar Expo
    log_info "Iniciando Mobile App (Expo)..."
    nohup npx expo start --tunnel > ../mobile-app.log 2>&1 &
    MOBILE_PID=$!
    log_success "✅ Mobile App iniciado (verifique QR code no log)"
    cd ..
else
    log_warning "⚠️ Mobile App não encontrado"
fi

# 7. Health Check Completo
echo ""
log_info "🏥 Health Check Completo"
echo "------------------------"

# API Core
if curl -s http://localhost:8080/api/actuator/health | grep -q "UP"; then
    log_success "✅ API Core - Health OK"
else
    log_error "❌ API Core - Health Check falhou"
fi

# Endpoints principais
if curl -s http://localhost:8080/api/employees > /dev/null; then
    log_success "✅ Endpoint /employees - OK"
else
    log_warning "⚠️ Endpoint /employees - Problema"
fi

# Métricas
if curl -s http://localhost:8080/api/actuator/prometheus | grep -q "jvm_memory"; then
    log_success "✅ Métricas Prometheus - OK"
else
    log_warning "⚠️ Métricas Prometheus - Não disponíveis"
fi

# Web Admin
if curl -s http://localhost:3001 > /dev/null 2>&1; then
    log_success "✅ Web Admin - Acessível"
else
    log_warning "⚠️ Web Admin - Não acessível ainda"
fi

# Prometheus
if curl -s http://localhost:9090/-/healthy > /dev/null 2>&1; then
    log_success "✅ Prometheus - Healthy"
else
    log_warning "⚠️ Prometheus - Não healthy"
fi

# Grafana
if curl -s http://localhost:3000/api/health > /dev/null 2>&1; then
    log_success "✅ Grafana - Healthy"
else
    log_warning "⚠️ Grafana - Não healthy"
fi

# 8. Teste de Integração Rápido
echo ""
log_info "⚡ Teste de Integração Rápido"
echo "-----------------------------"

# Criar employee de teste
log_info "Testando criação de employee..."
TEST_RESPONSE=$(curl -s -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"name":"Sistema Test","email":"sistema@test.com","rfidTag":"SYS001","active":true}')

if echo "$TEST_RESPONSE" | grep -q "Sistema Test"; then
    log_success "✅ Integração - CRUD funcionando"
    
    # Extrair ID e fazer check-in
    EMPLOYEE_ID=$(echo "$TEST_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2)
    CHECKIN_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/attendances/check-in" \
      -H "Content-Type: application/json" \
      -d "{\"employeeId\": $EMPLOYEE_ID}")
    
    if echo "$CHECKIN_RESPONSE" | grep -q "CHECK_IN"; then
        log_success "✅ Integração - Attendance funcionando"
    else
        log_warning "⚠️ Integração - Attendance com problema"
    fi
    
    # Limpar teste
    curl -s -X DELETE "http://localhost:8080/api/employees/$EMPLOYEE_ID" > /dev/null
else
    log_warning "⚠️ Integração - CRUD com problema"
fi

# RESULTADO FINAL
echo ""
echo "=========================================="
log_success "🎉 SISTEMA COMPLETO INICIADO!"
echo "=========================================="
echo ""
log_info "📊 Serviços Disponíveis:"
echo "  • API Core: http://localhost:8080/api"
echo "  • Swagger UI: http://localhost:8080/swagger-ui/index.html"
echo "  • Health Check: http://localhost:8080/api/actuator/health"
echo "  • Métricas: http://localhost:8080/api/actuator/prometheus"
echo "  • Web Admin: http://localhost:3001"
echo "  • Mobile App Web: http://localhost:19006"
echo "  • PostgreSQL: localhost:5432 (postgres/postgres)"
echo "  • MQTT Broker: localhost:1883"
echo "  • Prometheus: http://localhost:9090"
echo "  • Grafana: http://localhost:3000 (admin/admin)"
echo ""
log_info "🔧 Para parar todos os serviços:"
echo "  docker-compose down"
echo "  pkill -f 'spring-boot:run|SimulatorApplication|npm start|expo start'"
echo ""
log_info "🧪 Para executar testes E2E:"
echo "  ./run-e2e-integration.sh"
echo ""
log_info "📋 Logs dos serviços:"
echo "  • API Core: tail -f api-core.log"
echo "  • IoT Simulator: tail -f simulator.log"
echo "  • Web Admin: tail -f web-admin.log"
echo "  • Mobile App: tail -f mobile-app.log"