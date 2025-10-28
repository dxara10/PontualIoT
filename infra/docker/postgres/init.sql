-- Extensões necessárias
CREATE EXTENSION IF NOT EXISTS timescaledb;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Schema principal
CREATE SCHEMA IF NOT EXISTS pontual;

-- Tabelas principais
CREATE TABLE pontual.companies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    document VARCHAR(20) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pontual.employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES pontual.companies(id),
    name VARCHAR(255) NOT NULL,
    document VARCHAR(20) NOT NULL,
    employee_id VARCHAR(50) NOT NULL,
    position VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, employee_id)
);

CREATE TABLE pontual.devices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES pontual.companies(id),
    device_id VARCHAR(100) UNIQUE NOT NULL,
    model VARCHAR(100),
    firmware VARCHAR(50),
    ip_address INET,
    communication_method VARCHAR(20) CHECK (communication_method IN ('MQTT', 'HTTP', 'SDK')),
    location VARCHAR(255),
    status VARCHAR(20) DEFAULT 'OFFLINE' CHECK (status IN ('ONLINE', 'OFFLINE')),
    last_seen TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de eventos (hypertable para TimescaleDB)
CREATE TABLE pontual.attendance_events (
    id UUID DEFAULT uuid_generate_v4(),
    device_id UUID NOT NULL REFERENCES pontual.devices(id),
    employee_id UUID NOT NULL REFERENCES pontual.employees(id),
    event_type VARCHAR(20) NOT NULL CHECK (event_type IN ('CHECK_IN', 'CHECK_OUT', 'BREAK')),
    event_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    origin VARCHAR(10) NOT NULL CHECK (origin IN ('MQTT', 'HTTP', 'SDK')),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Converter para hypertable
SELECT create_hypertable('pontual.attendance_events', 'event_time');

-- Índices para performance
CREATE INDEX idx_attendance_events_device_time ON pontual.attendance_events (device_id, event_time DESC);
CREATE INDEX idx_attendance_events_employee_time ON pontual.attendance_events (employee_id, event_time DESC);
CREATE INDEX idx_devices_company ON pontual.devices (company_id);
CREATE INDEX idx_employees_company ON pontual.employees (company_id);