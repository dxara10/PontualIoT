#!/bin/bash
set -e

echo "🚀 Inicializando Smart Attendance Platform..."

# Verificar dependências
command -v docker >/dev/null 2>&1 || { echo "❌ Docker não encontrado"; exit 1; }
command -v docker-compose >/dev/null 2>&1 || { echo "❌ Docker Compose não encontrado"; exit 1; }

# Criar estruturas faltantes
echo "📁 Criando estruturas de diretórios..."
mkdir -p web-admin/{src,public} mobile-app/src

# Inicializar Git se não existir
if [ ! -d ".git" ]; then
    echo "🔧 Inicializando Git..."
    git init
    git add .
    git commit -m "feat: initial project structure with CI/CD pipeline"
fi

echo "✅ Projeto inicializado!"
echo "📋 Próximos passos:"
echo "  1. ./infra/scripts/test-all.sh - Executar testes"
echo "  2. ./infra/scripts/deploy-local.sh - Deploy local"
echo "  3. Implementar TDD nos serviços"