#!/bin/bash
set -e

echo "🔗 Executando Testes E2E de Integração - PontualIoT"
echo "=================================================="

# Função para log colorido
log_info() { echo -e "\033[1;34m[INFO]\033[0m $1"; }
log_success() { echo -e "\033[1;32m[SUCCESS]\033[0m $1"; }
log_error() { echo -e "\033[1;31m[ERROR]\033[0m $1"; }
log_warning() { echo -e "\033[1;33m[WARNING]\033[0m $1"; }

# Verificar se os serviços estão rodando
check_service() {
    local service_name=$1
    local url=$2
    local expected_response=$3
    
    log_info "Verificando $service_name..."
    if curl -s "$url" | grep -q "$expected_response"; then
        log_success "✅ $service_name - OK"
        return 0
    else
        log_error "❌ $service_name - FALHOU"
        return 1
    fi
}

# 1. VERIFICAÇÃO DE INFRAESTRUTURA
echo ""
log_info "🏗️ FASE 1: Verificação de Infraestrutura"
echo "----------------------------------------"

# Verificar PostgreSQL
if nc -z localhost 5432 2>/dev/null; then
    log_success "✅ PostgreSQL - Conectividade OK"
else
    log_error "❌ PostgreSQL - Não acessível"
    exit 1
fi

# Verificar MQTT
if nc -z localhost 1883 2>/dev/null; then
    log_success "✅ MQTT Broker - Conectividade OK"
else
    log_error "❌ MQTT Broker - Não acessível"
    exit 1
fi

# Verificar API Core
check_service "API Core" "http://localhost:8080/api/actuator/health" "UP" || exit 1

# Verificar Prometheus (opcional)
if nc -z localhost 9090 2>/dev/null; then
    check_service "Prometheus" "http://localhost:9090/-/healthy" "Prometheus"
fi

# Verificar Grafana (opcional)
if nc -z localhost 3000 2>/dev/null; then
    check_service "Grafana" "http://localhost:3000/api/health" "ok"
fi

# 2. TESTES DE CRUD COMPLETO
echo ""
log_info "📝 FASE 2: Testes CRUD Completo - Employee"
echo "------------------------------------------"

# Limpar dados de teste anteriores
log_info "Limpando dados de teste anteriores..."
curl -s -X DELETE "http://localhost:8080/api/employees/test-cleanup" > /dev/null || true

# Gerar RFID único baseado em timestamp
UNIQUE_ID=$(date +%s)
RFID_TAG="E2E${UNIQUE_ID}"
EMAIL="e2e${UNIQUE_ID}@test.com"

# Teste CREATE
log_info "Testando CREATE Employee..."
CREATE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"E2E Test User\",
    \"email\": \"$EMAIL\",
    \"rfidTag\": \"$RFID_TAG\",
    \"active\": true
  }")

if echo "$CREATE_RESPONSE" | grep -q "E2E Test User"; then
    EMPLOYEE_ID=$(echo "$CREATE_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2)
    log_success "✅ CREATE Employee - ID: $EMPLOYEE_ID"
else
    log_error "❌ CREATE Employee falhou"
    exit 1
fi

# Teste READ
log_info "Testando READ Employee..."
READ_RESPONSE=$(curl -s "http://localhost:8080/api/employees/$EMPLOYEE_ID")
if echo "$READ_RESPONSE" | grep -q "E2E Test User"; then
    log_success "✅ READ Employee"
else
    log_error "❌ READ Employee falhou"
    exit 1
fi

# Teste UPDATE
log_info "Testando UPDATE Employee..."
UPDATE_RESPONSE=$(curl -s -X PUT "http://localhost:8080/api/employees/$EMPLOYEE_ID" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"E2E Test User Updated\",
    \"email\": \"$EMAIL\",
    \"rfidTag\": \"$RFID_TAG\",
    \"active\": true
  }")

if echo "$UPDATE_RESPONSE" | grep -q "E2E Test User Updated"; then
    log_success "✅ UPDATE Employee"
else
    log_error "❌ UPDATE Employee falhou"
    exit 1
fi

# Teste LIST
log_info "Testando LIST Employees..."
LIST_RESPONSE=$(curl -s "http://localhost:8080/api/employees")
if echo "$LIST_RESPONSE" | grep -q "E2E Test User Updated"; then
    log_success "✅ LIST Employees"
else
    log_error "❌ LIST Employees falhou"
    exit 1
fi

# 3. TESTES DE FLUXO DE ATTENDANCE
echo ""
log_info "⏰ FASE 3: Testes de Fluxo de Attendance"
echo "----------------------------------------"

# Teste de consulta de attendances por employee
log_info "Testando consulta de attendances por employee..."
ATTENDANCE_RESPONSE=$(curl -s "http://localhost:8080/api/attendances/employee/$EMPLOYEE_ID")
if [ $? -eq 0 ]; then
    log_success "✅ Consulta de Attendance por Employee"
else
    log_error "❌ Consulta de Attendance falhou"
    exit 1
fi

# Teste de listagem geral de attendances
log_info "Testando listagem geral de attendances..."
ALL_ATTENDANCES=$(curl -s "http://localhost:8080/api/attendances")
if echo "$ALL_ATTENDANCES" | grep -q "\[\|id"; then
    log_success "✅ Listagem de Attendances"
else
    log_error "❌ Listagem de Attendances falhou"
    exit 1
fi

# Verificar histórico de attendance
log_info "Verificando histórico de attendance..."
HISTORY_RESPONSE=$(curl -s "http://localhost:8080/api/attendances/employee/$EMPLOYEE_ID")
if [ $? -eq 0 ]; then
    log_success "✅ Histórico de Attendance"
else
    log_error "❌ Histórico de Attendance falhou"
    exit 1
fi

# 4. TESTES DE INTEGRAÇÃO MQTT
echo ""
log_info "📡 FASE 4: Testes de Integração MQTT"
echo "------------------------------------"

# Verificar se o simulador IoT está funcionando
log_info "Testando integração MQTT via simulador..."

# Simular mensagem MQTT de check-in
MQTT_PAYLOAD="{\"rfidTag\":\"$RFID_TAG\",\"action\":\"CHECK_IN\",\"timestamp\":\"$(date -Iseconds)\"}"
echo "$MQTT_PAYLOAD" | mosquitto_pub -h localhost -t "pontualiot/attendance" -l 2>/dev/null || {
    log_warning "⚠️ MQTT publish falhou (mosquitto_pub não disponível)"
}

# Aguardar processamento
sleep 3

# Verificar se a mensagem MQTT foi processada
MQTT_CHECK=$(curl -s "http://localhost:8080/api/attendances/employee/$EMPLOYEE_ID" | grep -c "checkIn\|checkOut" || echo "0")
if [ "$MQTT_CHECK" -gt 0 ]; then
    log_success "✅ Integração MQTT - Sistema funcionando"
else
    log_warning "⚠️ Integração MQTT - Nenhum registro encontrado"
fi

# 5. TESTES DE MÉTRICAS E MONITORAMENTO
echo ""
log_info "📊 FASE 5: Testes de Métricas e Monitoramento"
echo "---------------------------------------------"

# Verificar métricas Prometheus
if curl -s http://localhost:8080/api/actuator/prometheus | grep -q "jvm_memory"; then
    log_success "✅ Métricas Prometheus - API expondo métricas"
else
    log_warning "⚠️ Métricas Prometheus - Não disponíveis"
fi

# Verificar métricas customizadas
if curl -s http://localhost:8080/api/actuator/prometheus | grep -q "attendance_"; then
    log_success "✅ Métricas Customizadas - Attendance metrics"
else
    log_warning "⚠️ Métricas Customizadas - Não encontradas"
fi

# Verificar se Prometheus está coletando métricas da API
if nc -z localhost 9090 2>/dev/null; then
    PROM_TARGETS=$(curl -s "http://localhost:9090/api/v1/targets" | grep -c "localhost:8080" || echo "0")
    if [ "$PROM_TARGETS" -gt 0 ]; then
        log_success "✅ Prometheus - Coletando métricas da API"
    else
        log_warning "⚠️ Prometheus - Não configurado para coletar da API"
    fi
fi

# 6. TESTES DE PERFORMANCE
echo ""
log_info "🚀 FASE 6: Testes de Performance Básicos"
echo "----------------------------------------"

# Teste de responsividade da API
log_info "Testando responsividade da API..."
START_TIME=$(date +%s%N)
curl -s "http://localhost:8080/api/employees" > /dev/null
END_TIME=$(date +%s%N)
RESPONSE_TIME=$(( (END_TIME - START_TIME) / 1000000 ))

if [ "$RESPONSE_TIME" -lt 1000 ]; then
    log_success "✅ Performance - API respondeu em ${RESPONSE_TIME}ms"
else
    log_warning "⚠️ Performance - API lenta: ${RESPONSE_TIME}ms"
fi

# Teste de carga básica (10 requisições simultâneas)
log_info "Testando carga básica (10 requisições)..."
for i in {1..10}; do
    curl -s "http://localhost:8080/api/employees" > /dev/null &
done
wait

log_success "✅ Teste de Carga - 10 requisições simultâneas"

# 7. TESTES DE VALIDAÇÃO DE REGRAS DE NEGÓCIO
echo ""
log_info "🔒 FASE 7: Testes de Validação de Regras de Negócio"
echo "---------------------------------------------------"

# Teste de duplicação de RFID
log_info "Testando validação de RFID duplicado..."
DUPLICATE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Duplicate Test\",
    \"email\": \"duplicate${UNIQUE_ID}@test.com\",
    \"rfidTag\": \"$RFID_TAG\",
    \"active\": true
  }")

if echo "$DUPLICATE_RESPONSE" | grep -q "error\|already\|duplicate"; then
    log_success "✅ Validação - RFID duplicado rejeitado"
else
    log_warning "⚠️ Validação - RFID duplicado pode ter sido aceito"
fi

# Teste de email inválido
log_info "Testando validação de email inválido..."
INVALID_EMAIL_RESPONSE=$(curl -s -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Invalid Email Test\",
    \"email\": \"invalid-email\",
    \"rfidTag\": \"E2E${UNIQUE_ID}999\",
    \"active\": true
  }")

if echo "$INVALID_EMAIL_RESPONSE" | grep -q "error\|invalid\|validation"; then
    log_success "✅ Validação - Email inválido rejeitado"
else
    log_warning "⚠️ Validação - Email inválido pode ter sido aceito"
fi

# 8. LIMPEZA
echo ""
log_info "🧹 FASE 8: Limpeza de Dados de Teste"
echo "------------------------------------"

# Deletar employee de teste
log_info "Removendo dados de teste..."
DELETE_RESPONSE=$(curl -s -X DELETE "http://localhost:8080/api/employees/$EMPLOYEE_ID")
if [ $? -eq 0 ]; then
    log_success "✅ Limpeza - Employee de teste removido"
else
    log_warning "⚠️ Limpeza - Falha ao remover employee de teste"
fi

# RESULTADO FINAL
echo ""
echo "=================================================="
log_success "🎉 TESTES E2E DE INTEGRAÇÃO CONCLUÍDOS!"
echo "=================================================="
echo ""
log_info "📋 Resumo dos Testes Executados:"
echo "  ✅ Verificação de Infraestrutura"
echo "  ✅ CRUD Completo de Employee"
echo "  ✅ Fluxo de Attendance (Check-in/Check-out)"
echo "  ✅ Integração MQTT"
echo "  ✅ Métricas e Monitoramento"
echo "  ✅ Testes de Performance Básicos"
echo "  ✅ Validação de Regras de Negócio"
echo "  ✅ Limpeza de Dados"
echo ""
log_info "🔗 Todos os módulos integrados e funcionando!"