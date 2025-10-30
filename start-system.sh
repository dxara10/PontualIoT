#!/bin/bash

echo "🚀 INICIANDO SISTEMA PONTUALIOT COMPLETO"
echo "========================================"
echo ""

# Função para aguardar serviço
wait_for_service() {
    local url=$1
    local name=$2
    local max_attempts=30
    local attempt=1
    
    echo "⏳ Aguardando $name..."
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
            echo "✅ $name está rodando!"
            return 0
        fi
        echo "   Tentativa $attempt/$max_attempts..."
        sleep 2
        attempt=$((attempt + 1))
    done
    echo "❌ $name não iniciou"
    return 1
}

# 1. Iniciar API Core
echo "1. 🔧 Iniciando API Core..."
cd api-core
mvn spring-boot:run -Dspring-boot.run.profiles=local > /dev/null 2>&1 &
API_PID=$!
cd ..

# Aguardar API iniciar
wait_for_service "http://localhost:8080/api/actuator/health" "API Core"

# 2. Injetar dados de exemplo
echo ""
echo "2. 💉 Injetando dados de exemplo..."
curl -s -X POST http://localhost:8080/api/employees -H "Content-Type: application/json" -d '{"name":"João Silva","email":"joao@pontualiot.com","rfidTag":"RFID001","active":true}' > /dev/null
curl -s -X POST http://localhost:8080/api/employees -H "Content-Type: application/json" -d '{"name":"Maria Santos","email":"maria@pontualiot.com","rfidTag":"RFID002","active":true}' > /dev/null
curl -s -X POST http://localhost:8080/api/employees -H "Content-Type: application/json" -d '{"name":"Carlos Lima","email":"carlos@pontualiot.com","rfidTag":"RFID003","active":false}' > /dev/null

EMPLOYEE_COUNT=$(curl -s http://localhost:8080/api/employees | jq length)
echo "✅ $EMPLOYEE_COUNT funcionários criados"

# 3. Iniciar Web Admin
echo ""
echo "3. 🌐 Iniciando Web Admin..."
cd web-admin
PORT=3001 npm start > /dev/null 2>&1 &
WEB_PID=$!
cd ..

# Aguardar Web Admin iniciar
wait_for_service "http://localhost:3001" "Web Admin"

echo ""
echo "🎉 SISTEMA INICIADO COM SUCESSO!"
echo "================================"
echo ""
echo "🔗 ACESSE:"
echo "• Web Admin: http://localhost:3001"
echo "• API Swagger: http://localhost:8080/swagger-ui/index.html"
echo "• Grafana: http://localhost:3000 (admin/admin)"
echo ""
echo "📊 DADOS DISPONÍVEIS:"
echo "• $EMPLOYEE_COUNT funcionários cadastrados"
echo "• Navegue entre as abas Dashboard e Funcionários"
echo ""
echo "🛑 PARA PARAR:"
echo "kill $API_PID $WEB_PID"