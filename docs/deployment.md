# Deployment Guide - PontualIoT

## 🎯 Opções de Deploy

### **1. Local Development**
```bash
# Quick start
docker-compose up -d postgres
cd api-core && mvn spring-boot:run
```

### **2. Docker Compose (Recomendado)**
```bash
# Deploy completo
docker-compose up -d

# Apenas infraestrutura
docker-compose up -d postgres
```

### **3. Kubernetes**
```bash
# Deploy no cluster
kubectl apply -f infra/k8s/

# Verificar pods
kubectl get pods -l app=pontualiot
```

### **4. Cloud Providers**

#### **AWS ECS**
```bash
# Deploy via CLI
aws ecs update-service --cluster pontualiot --service api-core
```

#### **Google Cloud Run**
```bash
# Deploy serverless
gcloud run deploy pontualiot-api --image gcr.io/project/api-core
```

#### **Azure Container Instances**
```bash
# Deploy container
az container create --resource-group pontualiot --name api-core
```

## 🌍 Ambientes

### **Development**
- **URL**: http://localhost:8082
- **Database**: PostgreSQL local
- **Profile**: `local`

### **Staging**
- **URL**: https://staging.pontualiot.com
- **Database**: PostgreSQL cloud
- **Profile**: `staging`

### **Production**
- **URL**: https://api.pontualiot.com
- **Database**: PostgreSQL HA
- **Profile**: `production`

## 🔧 Configuração por Ambiente

### **Environment Variables**
```bash
# Development
export SPRING_PROFILES_ACTIVE=local
export DB_HOST=localhost

# Staging
export SPRING_PROFILES_ACTIVE=staging
export DB_HOST=staging-db.pontualiot.com

# Production
export SPRING_PROFILES_ACTIVE=production
export DB_HOST=prod-db.pontualiot.com
```

## 📊 Monitoramento

### **Health Checks**
```bash
# API Health
curl https://api.pontualiot.com/actuator/health

# Database
curl https://api.pontualiot.com/actuator/health/db
```

### **Metrics**
- **Prometheus**: `/actuator/prometheus`
- **Grafana**: Dashboard customizado
- **Logs**: Centralizados via ELK Stack

## 🔒 Segurança

### **HTTPS**
- **Certificados**: Let's Encrypt
- **Proxy**: Nginx/Traefik
- **Headers**: Security headers

### **Database**
- **SSL**: Conexões criptografadas
- **Backup**: Automático diário
- **Access**: IP whitelist

## 🚀 Deploy Automatizado

### **GitHub Actions**
```yaml
# Trigger automático no push para main
on:
  push:
    branches: [main]
```

### **Rollback**
```bash
# Rollback automático em caso de falha
git revert HEAD
git push origin main
```