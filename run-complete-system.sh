#!/bin/bash

echo "🚀 Iniciando Sistema Completo PontualIoT..."

# Subir infraestrutura
echo "📊 Subindo stack de monitoramento..."
docker-compose up -d

echo "⏳ Aguardando serviços iniciarem..."
sleep 15

# Compilar e rodar API Core
echo "🔧 Compilando API Core..."
cd api-core
mvn clean compile -q

echo "🌐 Iniciando API Core..."
mvn spring-boot:run &
API_PID=$!
cd ..

echo "⏳ Aguardando API Core inicializar..."
sleep 20

# Compilar e rodar IoT Simulator
echo "📡 Compilando IoT Simulator..."
cd iot-devices/simulator
mvn clean compile -q

echo "🤖 Iniciando IoT Simulator..."
mvn exec:java -Dexec.mainClass="com.pontualiot.simulator.SimulatorApplication" &
SIMULATOR_PID=$!
cd ../..

echo ""
echo "✅ Sistema PontualIoT iniciado com sucesso!"
echo ""
echo "📊 Dashboards disponíveis:"
echo "  • Grafana: http://localhost:3000 (admin/admin)"
echo "  • Prometheus: http://localhost:9090"
echo "  • API Swagger: http://localhost:8082/swagger-ui.html"
echo ""
echo "🔴 Monitoramento em tempo real:"
echo "  • Métricas: http://localhost:8082/actuator/prometheus"
echo "  • Health: http://localhost:8082/actuator/health"
echo ""
echo "📡 MQTT:"
echo "  • Broker: localhost:1883"
echo "  • WebSocket: localhost:9001"
echo ""
echo "🛑 Para parar o sistema: Ctrl+C"

# Trap para cleanup
trap 'echo "🛑 Parando sistema..."; kill $API_PID $SIMULATOR_PID 2>/dev/null; docker-compose down; exit' INT

# Manter script rodando
wait