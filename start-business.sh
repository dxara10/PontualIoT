#!/bin/bash
set -e

echo "🏢 Iniciando Módulos de Negócio - PontualIoT"
echo "============================================"

# Função para log colorido
log_info() { echo -e "\033[1;34m[INFO]\033[0m $1"; }
log_success() { echo -e "\033[1;32m[SUCCESS]\033[0m $1"; }
log_error() { echo -e "\033[1;31m[ERROR]\033[0m $1"; }
log_warning() { echo -e "\033[1;33m[WARNING]\033[0m $1"; }

# Verificar se Docker está rodando
if ! docker info > /dev/null 2>&1; then
    log_error "❌ Docker não está rodando. Inicie o Docker primeiro."
    exit 1
fi

# 1. Subir infraestrutura básica
log_info "🗄️ Subindo infraestrutura (PostgreSQL + MQTT)..."
if docker-compose up -d postgres mosquitto; then
    log_success "✅ Infraestrutura rodando"
else
    log_error "❌ Falha ao subir infraestrutura"
    exit 1
fi

# Aguardar serviços
log_info "⏳ Aguardando infraestrutura inicializar..."
sleep 8

# 2. Iniciar API Core
log_info "🌐 Iniciando API Core..."
cd api-core
if mvn clean compile -q; then
    nohup mvn spring-boot:run > ../api-core.log 2>&1 &
    API_PID=$!
    log_success "✅ API Core compilada e iniciada"
else
    log_error "❌ Falha ao compilar API Core"
    exit 1
fi
cd ..

# Aguardar API inicializar
log_info "⏳ Aguardando API Core inicializar..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
        log_success "✅ API Core rodando na porta 8080"
        break
    fi
    sleep 2
done

# 3. Iniciar IoT Simulator
log_info "📡 Iniciando IoT Simulator..."
cd iot-devices/simulator
if mvn clean compile -q; then
    nohup mvn exec:java -Dexec.mainClass="com.pontualiot.simulator.SimulatorApplication" > ../../simulator.log 2>&1 &
    SIMULATOR_PID=$!
    log_success "✅ IoT Simulator compilado e iniciado"
else
    log_warning "⚠️ Falha ao compilar IoT Simulator (continuando...)"
fi
cd ../..

# 4. Iniciar Web Admin (se disponível)
if [ -f "web-admin/package.json" ]; then
    log_info "🖥️ Iniciando Web Admin..."
    cd web-admin
    if [ -d "node_modules" ]; then
        nohup npm start > ../web-admin.log 2>&1 &
        WEB_PID=$!
        log_success "✅ Web Admin iniciado"
    else
        log_info "📦 Instalando dependências do Web Admin..."
        if npm install; then
            nohup npm start > ../web-admin.log 2>&1 &
            WEB_PID=$!
            log_success "✅ Web Admin instalado e iniciado"
        else
            log_warning "⚠️ Falha ao instalar Web Admin (continuando...)"
        fi
    fi
    cd ..
fi

# 5. Iniciar Mobile App (se disponível)
if [ -f "mobile-app/package.json" ]; then
    log_info "📱 Iniciando Mobile App..."
    cd mobile-app
    if [ -d "node_modules" ]; then
        nohup npx expo start --tunnel > ../mobile-app.log 2>&1 &
        MOBILE_PID=$!
        log_success "✅ Mobile App iniciado (Web + Android/iOS)"
    else
        log_info "📦 Instalando dependências do Mobile App..."
        if npm install; then
            nohup npx expo start --tunnel > ../mobile-app.log 2>&1 &
            MOBILE_PID=$!
            log_success "✅ Mobile App instalado e iniciado"
        else
            log_warning "⚠️ Falha ao instalar Mobile App (continuando...)"
        fi
    fi
    cd ..
fi

# 5. Aguardar todos os serviços
log_info "⏳ Aguardando todos os serviços estabilizarem..."
sleep 10

echo ""
echo "============================================"
log_success "🎉 MÓDULOS DE NEGÓCIO ATIVOS!"
echo "============================================"
echo ""
log_info "🏢 Serviços de Negócio:"
echo "  • API Core: http://localhost:8080/api"
echo "  • Swagger: http://localhost:8080/swagger-ui/index.html"
echo "  • Health: http://localhost:8080/api/actuator/health"
echo "  • IoT Simulator: Rodando em background"
echo "  • Web Admin: http://localhost:3001 (se disponível)"
echo "  • Mobile App Web: http://localhost:19006"
echo "  • Mobile App Android/iOS: Escaneie QR code"
echo ""
log_info "🗄️ Infraestrutura:"
echo "  • PostgreSQL: localhost:5432"
echo "  • MQTT Broker: localhost:1883"
echo "  • MQTT WebSocket: localhost:9001"
echo ""
log_info "🔧 Para parar os serviços:"
echo "  docker-compose stop postgres mosquitto"
echo "  pkill -f 'spring-boot:run|SimulatorApplication|npm start|expo start'"
echo ""
log_warning "💡 Para monitoramento, execute: ./start-monitoring.sh"