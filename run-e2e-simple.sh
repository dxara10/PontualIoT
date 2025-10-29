#!/bin/bash
set -e

echo "🔧 Executando Testes E2E Simplificados"
echo "====================================="

# Limpar ambiente
echo "🧹 Limpando ambiente..."
pkill -f "spring-boot:run" 2>/dev/null || true
docker stop $(docker ps -q) 2>/dev/null || true

# Subir apenas PostgreSQL
echo "🐘 Subindo PostgreSQL..."
docker run -d --name test-postgres \
  -e POSTGRES_DB=pontualiot \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:14

sleep 8

# Iniciar API em background
echo "🚀 Iniciando API..."
cd api-core
timeout 60s mvn spring-boot:run > ../api-test.log 2>&1 &
API_PID=$!
cd ..

# Aguardar API
echo "⏳ Aguardando API..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
        echo "✅ API disponível"
        break
    fi
    sleep 2
    if [ $i -eq 30 ]; then
        echo "❌ API não inicializou"
        cat api-test.log | tail -20
        exit 1
    fi
done

# Executar testes E2E básicos
echo "🧪 Executando testes E2E..."
cd e2e-tests
mvn test -Dtest="SystemIntegrationTest" -q
RESULT=$?
cd ..

# Cleanup
echo "🧹 Limpeza final..."
kill $API_PID 2>/dev/null || true
docker stop test-postgres 2>/dev/null || true
docker rm test-postgres 2>/dev/null || true

if [ $RESULT -eq 0 ]; then
    echo "🎉 TESTES E2E PASSARAM!"
else
    echo "💥 TESTES E2E FALHARAM"
fi

exit $RESULT