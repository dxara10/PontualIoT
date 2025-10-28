# Design de API - Smart Attendance Platform

## 🔐 Autenticação
```
POST /api/auth/login
Authorization: Bearer {jwt_token}
```

## 📊 Endpoints Principais

### Companies
```
GET    /api/companies          # Listar empresas (ADMIN only)
POST   /api/companies          # Criar empresa (ADMIN only)
GET    /api/companies/{id}     # Detalhes da empresa
PUT    /api/companies/{id}     # Atualizar empresa
```

### Employees
```
GET    /api/employees          # Listar funcionários da empresa
POST   /api/employees          # Cadastrar funcionário
GET    /api/employees/{id}     # Detalhes do funcionário
PUT    /api/employees/{id}     # Atualizar funcionário
DELETE /api/employees/{id}     # Desativar funcionário
```

### Devices
```
GET    /api/devices            # Listar dispositivos da empresa
POST   /api/devices            # Registrar dispositivo
GET    /api/devices/{id}       # Detalhes do dispositivo
PUT    /api/devices/{id}       # Atualizar dispositivo
DELETE /api/devices/{id}       # Desativar dispositivo
GET    /api/devices/{id}/status # Status em tempo real
```

### Attendance Events
```
GET    /api/events             # Consultar eventos (com filtros)
POST   /api/events             # Registrar evento (HTTP devices)
GET    /api/events/{id}        # Detalhes do evento
GET    /api/events/report      # Relatório agregado
```

### Device Commands
```
GET    /api/commands           # Listar comandos
POST   /api/commands           # Enviar comando
GET    /api/commands/{id}      # Status do comando
```

### Monitoring
```
GET    /api/dashboard          # Dashboard data
GET    /api/health             # Health check
GET    /api/metrics            # Prometheus metrics
```

## 📝 Payloads de Exemplo

### Registrar Evento (POST /api/events)
```json
{
  "deviceId": "uuid",
  "employeeRegistration": "12345",
  "eventType": "CHECK_IN",
  "eventTime": "2024-01-15T08:00:00Z",
  "metadata": {
    "biometricScore": 0.95,
    "cardId": "ABC123",
    "temperature": 36.5
  }
}
```

### Enviar Comando (POST /api/commands)
```json
{
  "deviceId": "uuid",
  "commandType": "SYNC_TIME",
  "payload": {
    "timezone": "America/Sao_Paulo",
    "ntpServer": "pool.ntp.org"
  }
}
```

### Filtros de Consulta (GET /api/events)
```
?employeeId=uuid
&deviceId=uuid
&startDate=2024-01-01
&endDate=2024-01-31
&eventType=CHECK_IN
&page=0
&size=50
```

## 🔄 WebSocket Events
```
/ws/dashboard - Real-time dashboard updates
/ws/devices/{id} - Device status updates
/ws/alerts - System alerts
```

## 📱 Mobile API Extensions
```
GET    /api/mobile/profile     # Employee profile
POST   /api/mobile/checkin     # Mobile check-in
GET    /api/mobile/history     # Personal attendance history
```