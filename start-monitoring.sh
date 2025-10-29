#!/bin/bash

echo "🚀 Iniciando Stack de Monitoramento PontualIoT..."

# Subir todos os serviços
docker-compose up -d

echo "⏳ Aguardando serviços iniciarem..."
sleep 10

echo "✅ Serviços disponíveis:"
echo "📊 Grafana: http://localhost:3000 (admin/admin)"
echo "📈 Prometheus: http://localhost:9090"
echo "🔌 API Core: http://localhost:8082"
echo "📡 MQTT Broker: localhost:1883"
echo "🌐 MQTT WebSocket: localhost:9001"

echo ""
echo "🔴 Para ver fluxo em tempo real:"
echo "1. Acesse Grafana: http://localhost:3000"
echo "2. Login: admin/admin"
echo "3. Dashboard: PontualIoT - Real Time Monitoring"
echo ""
echo "📊 Para testar métricas:"
echo "curl http://localhost:8082/api/attendances"