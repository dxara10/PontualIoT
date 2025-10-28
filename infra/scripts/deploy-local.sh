#!/bin/bash
set -e

echo "🚀 Deploy local do ambiente completo..."

# Build dos serviços
echo "🔨 Building services..."
docker-compose build

# Subir infraestrutura
echo "📦 Subindo infraestrutura..."
docker-compose up -d postgres mosquitto redis

# Aguardar serviços ficarem prontos
echo "⏳ Aguardando serviços..."
sleep 10

# Subir aplicações
echo "🚀 Subindo aplicações..."
docker-compose up -d

echo "✅ Deploy concluído!"
echo "📊 Acesse:"
echo "  - API: http://localhost:8080"
echo "  - Web Admin: http://localhost:3000"
echo "  - Grafana: http://localhost:3001"
echo "  - Prometheus: http://localhost:9090"