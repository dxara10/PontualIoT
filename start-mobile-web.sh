#!/bin/bash

echo "🚀 Iniciando PontualIoT Mobile App no navegador..."

cd mobile-app

# Instalar dependências se necessário
if [ ! -d "node_modules" ]; then
    echo "📦 Instalando dependências..."
    npm install --legacy-peer-deps
fi

# Iniciar app web
echo "🌐 Iniciando servidor web na porta 19007..."
npx expo start --web --port 19007

echo "✅ App disponível em: http://localhost:19007"