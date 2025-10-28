#!/bin/bash
set -e

echo "🧪 Executando todos os testes..."

# Testes Java (API Core)
echo "📋 Testando API Core..."
cd api-core && mvn clean test && cd ..

# Testes Java (IoT Simulator)
echo "📋 Testando IoT Simulator..."
cd iot-devices/simulator && mvn clean test && cd ../..

# Testes Frontend
echo "📋 Testando Web Admin..."
cd web-admin && npm test -- --coverage --watchAll=false && cd ..

echo "✅ Todos os testes passaram!"