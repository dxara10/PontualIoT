#!/bin/bash
set -e

echo "📊 Iniciando Stack de Monitoramento - PontualIoT"
echo "================================================"

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

# Subir stack de monitoramento
log_info "🚀 Subindo stack de monitoramento..."
if docker-compose up -d prometheus grafana; then
    log_success "✅ Stack de monitoramento iniciada"
else
    log_error "❌ Falha ao subir stack de monitoramento"
    exit 1
fi

# Aguardar serviços inicializarem
log_info "⏳ Aguardando serviços inicializarem..."
sleep 15

# Verificar Prometheus
log_info "🔍 Verificando Prometheus..."
for i in {1..10}; do
    if curl -s http://localhost:9090/-/healthy > /dev/null 2>&1; then
        log_success "✅ Prometheus rodando na porta 9090"
        break
    fi
    sleep 2
done

# Verificar Grafana
log_info "📈 Verificando Grafana..."
for i in {1..10}; do
    if curl -s http://localhost:3000/api/health > /dev/null 2>&1; then
        log_success "✅ Grafana rodando na porta 3000"
        break
    fi
    sleep 2
done

echo ""
echo "================================================"
log_success "🎉 STACK DE MONITORAMENTO ATIVA!"
echo "================================================"
echo ""
log_info "📊 Dashboards Disponíveis:"
echo "  • Grafana: http://localhost:3000 (admin/admin)"
echo "  • Prometheus: http://localhost:9090"
echo "  • Métricas API: http://localhost:8080/api/actuator/prometheus"
echo ""
log_info "🔧 Para parar o monitoramento:"
echo "  docker-compose stop prometheus grafana"
echo ""
log_warning "💡 Dica: Inicie a API Core primeiro para coletar métricas!"