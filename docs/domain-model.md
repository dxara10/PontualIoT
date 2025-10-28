# Modelo de Domínio - Smart Attendance Platform

## 📊 Diagrama de Classes (Conceitual)

```
┌─────────────────┐    1:N    ┌─────────────────┐
│     Company     │◄──────────│    Employee     │
│                 │           │                 │
│ + id: UUID      │           │ + id: UUID      │
│ + name: String  │           │ + fullName      │
│ + document      │           │ + registration  │
│ + active        │           │ + cpf           │
└─────────────────┘           │ + position      │
         │                    │ + active        │
         │ 1:N                └─────────────────┘
         ▼                             │
┌─────────────────┐                    │ N:M (events)
│     Device      │                    │
│                 │                    ▼
│ + id: UUID      │           ┌─────────────────┐
│ + serialNumber  │◄──────────│ AttendanceEvent │
│ + model         │    N:1    │                 │
│ + firmware      │           │ + id: UUID      │
│ + commMode      │           │ + eventTime     │
│ + ipAddress     │           │ + eventType     │
│ + location      │           │ + metadata      │
│ + enabled       │           │ + origin        │
│ + active        │           └─────────────────┘
└─────────────────┘
         │
         │ 1:N
         ▼
┌─────────────────┐
│  DeviceCommand  │
│                 │
│ + id: UUID      │
│ + commandType   │
│ + payload       │
│ + status        │
│ + originUser    │
└─────────────────┘
```

## 🎯 Principais Relacionamentos

### Company (1:N)
- **Employees**: Uma empresa tem muitos funcionários
- **Devices**: Uma empresa tem muitos dispositivos  
- **ApiUsers**: Uma empresa tem muitos usuários do sistema

### Device (1:N)
- **AttendanceEvents**: Um dispositivo registra muitos eventos
- **DeviceEvents**: Um dispositivo gera muitos eventos de telemetria
- **DeviceCommands**: Um dispositivo recebe muitos comandos

### Employee (1:N)
- **AttendanceEvents**: Um funcionário tem muitos registros de ponto

## 🔐 Multitenancy
- **Isolamento por Company**: Todos os dados são isolados por empresa
- **Índices otimizados**: `company_id` em todas as consultas principais
- **Segurança**: Usuários só acessam dados da própria empresa

## 📈 Time-Series (TimescaleDB)
- **AttendanceEvents**: Hypertable particionada por `event_time`
- **DeviceEvents**: Hypertable para telemetria de dispositivos
- **Retenção**: Políticas automáticas de retenção de dados antigos