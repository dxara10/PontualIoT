# CI/CD Pipeline - PontualIoT

## 🎯 Visão Geral

A esteira CI/CD do PontualIoT automatiza testes, build, deploy e validação da aplicação em múltiplas plataformas.

## 🏗️ Arquitetura da Pipeline

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   STAGE 1   │    │   STAGE 2   │    │   STAGE 3   │    │   STAGE 4   │
│    Test     │───▶│    Build    │───▶│   Docker    │───▶│   Deploy    │
│             │    │             │    │             │    │             │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                                                                │
┌─────────────┐    ┌─────────────┐                            │
│   STAGE 6   │    │   STAGE 5   │                            │
│ Smoke Tests │◀───│ Health Check│◀───────────────────────────┘
│             │    │             │
└─────────────┘    └─────────────┘
```

## 🚀 Como Executar

### **Local**
```bash
# Executar esteira completa
bash run-ci-cd.sh

# Executar stages individuais
./infra/scripts/test-all.sh
./infra/scripts/deploy-local.sh
```

### **GitHub Actions** (Automático)
```bash
# Trigger automático
git push origin main
git push origin develop

# Ver execução
https://github.com/seu-usuario/PontualIoT/actions
```

## 📋 Stages Detalhados

### **STAGE 1: Test**
- **API Core**: `mvn clean test`
- **IoT Simulator**: `mvn clean test`
- **Cobertura**: Relatórios de teste
- **Duração**: ~2-3 min

### **STAGE 2: Build & Package**
- **API Core**: `mvn clean package`
- **Artefatos**: JAR executável
- **Validação**: Build sem erros
- **Duração**: ~1-2 min

### **STAGE 3: Docker Build**
- **Images**: api-core, iot-simulator
- **Registry**: GitHub Container Registry
- **Tags**: latest, version
- **Duração**: ~2-3 min

### **STAGE 4: Deploy**
- **PostgreSQL**: Container Docker
- **API Core**: Spring Boot
- **Ports**: 8082 (API), 5432 (DB)
- **Duração**: ~1-2 min

### **STAGE 5: Health Check**
- **API Health**: `/actuator/health`
- **Endpoints**: Verificação básica
- **Database**: Conectividade
- **Duração**: ~30s

### **STAGE 6: Smoke Tests**
- **CRUD**: Criação de employee
- **Endpoints**: Testes básicos
- **Integration**: API + DB
- **Duração**: ~30s

## 🌐 Plataformas Suportadas

### **1. GitHub Actions** ✅ (Configurado)
```yaml
# .github/workflows/ci-cd.yml
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]
```
- **Gratuito**: 2000 min/mês
- **Runners**: Ubuntu, Windows, macOS
- **Integração**: GitHub nativo

### **2. GitLab CI/CD**
```yaml
# .gitlab-ci.yml (criar se necessário)
stages:
  - test
  - build
  - deploy

test:
  image: openjdk:21
  script:
    - bash run-ci-cd.sh
```
- **Gratuito**: 400 min/mês
- **Runners**: Próprios ou compartilhados

### **3. Jenkins**
```groovy
# Jenkinsfile (criar se necessário)
pipeline {
    agent any
    stages {
        stage('CI/CD') {
            steps {
                sh 'bash run-ci-cd.sh'
            }
        }
    }
}
```
- **Self-hosted**: Controle total
- **Plugins**: Extensível

### **4. Cloud Providers**

#### **AWS CodePipeline**
```yaml
# buildspec.yml
version: 0.2
phases:
  build:
    commands:
      - bash run-ci-cd.sh
```

#### **Google Cloud Build**
```yaml
# cloudbuild.yaml
steps:
  - name: 'maven:3.8-openjdk-21'
    entrypoint: 'bash'
    args: ['run-ci-cd.sh']
```

#### **Azure DevOps**
```yaml
# azure-pipelines.yml
trigger: [main]
pool:
  vmImage: 'ubuntu-latest'
steps:
  - script: bash run-ci-cd.sh
```

## 🔧 Configuração

### **Variáveis de Ambiente**
```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=pontualiot
DB_USER=postgres
DB_PASSWORD=postgres

# Application
SERVER_PORT=8082
SPRING_PROFILES_ACTIVE=local
```

### **Secrets (GitHub)**
```bash
# GitHub Repository Settings > Secrets
GITHUB_TOKEN=ghp_xxx  # Automático
DB_PASSWORD=postgres  # Se necessário
```

## 📊 Monitoramento

### **Métricas**
- **Build Time**: Tempo total da pipeline
- **Test Coverage**: Cobertura de testes
- **Success Rate**: Taxa de sucesso
- **Deployment Frequency**: Frequência de deploy

### **Logs**
```bash
# Local
tail -f api-core/logs/application.log

# GitHub Actions
# Ver na interface web do GitHub
```

### **Alertas**
- **Build Failed**: Notificação automática
- **Tests Failed**: Email/Slack
- **Deploy Failed**: Rollback automático

## 🛠️ Troubleshooting

### **Problemas Comuns**

#### **Testes Falhando**
```bash
# Executar localmente
cd api-core && mvn test
# Verificar logs específicos
```

#### **Build Falhando**
```bash
# Limpar cache
mvn clean
# Verificar dependências
mvn dependency:tree
```

#### **Deploy Falhando**
```bash
# Verificar PostgreSQL
docker-compose ps postgres
# Verificar portas
netstat -tulpn | grep 8082
```

#### **Health Check Falhando**
```bash
# Verificar API
curl http://localhost:8082/api/actuator/health
# Verificar logs
docker-compose logs api-core
```

## 📈 Melhorias Futuras

### **Próximas Implementações**
- [ ] **Testes E2E**: Cypress/Selenium
- [ ] **Security Scan**: SAST/DAST
- [ ] **Performance Tests**: JMeter
- [ ] **Multi-environment**: Dev/Staging/Prod
- [ ] **Blue-Green Deploy**: Zero downtime
- [ ] **Monitoring**: Prometheus/Grafana

### **Otimizações**
- [ ] **Cache**: Dependencies caching
- [ ] **Parallel**: Testes paralelos
- [ ] **Artifacts**: Reuso de builds
- [ ] **Notifications**: Slack/Teams

## 🎯 Comandos Úteis

```bash
# Executar pipeline local
bash run-ci-cd.sh

# Executar apenas testes
./infra/scripts/test-all.sh

# Deploy local
./infra/scripts/deploy-local.sh

# Parar serviços
docker-compose down
pkill -f spring-boot:run

# Ver logs
docker-compose logs -f postgres
tail -f api-core/logs/application.log

# Health check manual
curl http://localhost:8082/api/actuator/health
```

## 📞 Suporte

- **Documentação**: `/docs`
- **Issues**: GitHub Issues
- **Logs**: `/logs` directory
- **Monitoring**: Swagger UI