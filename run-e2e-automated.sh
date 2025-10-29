#!/bin/bash
set -e

echo "🚀 Executando Testes E2E Automatizados - PontualIoT"
echo "=================================================="

# Função para log colorido
log_info() { echo -e "\033[1;34m[INFO]\033[0m $1"; }
log_success() { echo -e "\033[1;32m[SUCCESS]\033[0m $1"; }
log_error() { echo -e "\033[1;31m[ERROR]\033[0m $1"; }

# 1. Limpar processos existentes
log_info "🧹 Limpando processos existentes..."
pkill -f "spring-boot:run" 2>/dev/null || true
lsof -ti:8080,1883 | xargs kill -9 2>/dev/null || true

# 2. Subir infraestrutura
log_info "🐳 Subindo infraestrutura..."
docker-compose up -d postgres mosquitto
sleep 5

# 3. Iniciar API
log_info "🚀 Iniciando API Core..."
cd api-core
nohup mvn spring-boot:run > ../api-e2e.log 2>&1 &
API_PID=$!
cd ..

# 4. Aguardar API inicializar
log_info "⏳ Aguardando API inicializar..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
        log_success "✅ API rodando"
        break
    fi
    sleep 2
    if [ $i -eq 30 ]; then
        log_error "❌ API não inicializou"
        exit 1
    fi
done

# 5. Executar testes E2E
log_info "🧪 Executando testes E2E..."
cd e2e-tests
if mvn test; then
    log_success "✅ Testes E2E passaram"
    RESULT=0
else
    log_error "❌ Testes E2E falharam"
    RESULT=1
fi
cd ..

# 6. Cleanup
log_info "🧹 Limpando ambiente..."
kill $API_PID 2>/dev/null || true
docker-compose down

echo "=================================================="
if [ $RESULT -eq 0 ]; then
    log_success "🎉 TESTES E2E EXECUTADOS COM SUCESSO!"
else
    log_error "💥 TESTES E2E FALHARAM"
fi
echo "=================================================="

exit $RESULT