#!/bin/bash

echo "🧪 Executando Testes E2E - PontualIoT"
echo "======================================"

# Verificar se o sistema está rodando
echo ""
echo "🔍 1. Verificando se o sistema está rodando..."

# Verificar API Core
if curl -s http://localhost:8082/api/actuator/health > /dev/null; then
    echo "✅ API Core: Rodando"
else
    echo "❌ API Core: Não está rodando"
    echo "💡 Execute: ./run-complete-system.sh"
    exit 1
fi

# Verificar Prometheus
if curl -s http://localhost:9090/api/v1/query?query=up > /dev/null; then
    echo "✅ Prometheus: Rodando"
else
    echo "❌ Prometheus: Não está rodando"
    exit 1
fi

# Verificar Grafana
if curl -s http://localhost:3000/api/health > /dev/null; then
    echo "✅ Grafana: Rodando"
else
    echo "❌ Grafana: Não está rodando"
    exit 1
fi

echo ""
echo "🧪 2. Executando Testes de Integração do Sistema..."
cd e2e-tests
mvn test -Dtest=SystemIntegrationTest -q

if [ $? -eq 0 ]; then
    echo "✅ Testes de Integração: PASSOU"
else
    echo "❌ Testes de Integração: FALHOU"
    exit 1
fi

echo ""
echo "🚀 3. Executando Testes E2E Completos..."
mvn test -Dtest=PontualIoTEndToEndTest -q

if [ $? -eq 0 ]; then
    echo "✅ Testes E2E: PASSOU"
    echo ""
    echo "🎉 TODOS OS TESTES PASSARAM!"
    echo ""
    echo "📊 Fluxo testado:"
    echo "  1. ✅ Sistema saudável"
    echo "  2. ✅ Criação de funcionário"
    echo "  3. ✅ Evento MQTT CHECK_IN"
    echo "  4. ✅ Evento MQTT CHECK_OUT"
    echo "  5. ✅ Métricas atualizadas"
    echo "  6. ✅ Prometheus coletando"
    echo "  7. ✅ Grafana acessível"
else
    echo "❌ Testes E2E: FALHOU"
    echo ""
    echo "📋 Para debug, verifique:"
    echo "  • Logs da API: tail -f api-core.log"
    echo "  • Logs do Simulator: tail -f simulator.log"
    echo "  • MQTT funcionando: mosquitto_pub -h localhost -p 1883 -t test -m hello"
    exit 1
fi

cd ..
echo ""
echo "🎯 Sistema PontualIoT validado end-to-end!"