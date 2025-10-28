-- Production schema (Flyway migration would be better)
CREATE EXTENSION IF NOT EXISTS timescaledb;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Schema principal
CREATE SCHEMA IF NOT EXISTS pontual;

-- Tabelas principais
CREATE TABLE IF NOT EXISTS pontual.companies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    document VARCHAR(20) UNIQUE NOT NULL,
    address TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pontual.api_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES pontual.companies(id),
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'OPERATOR', 'VIEWER')),
    active BOOLEAN NOT NULL DEFAULT true,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pontual.employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES pontual.companies(id),
    full_name VARCHAR(255) NOT NULL,
    registration VARCHAR(50) NOT NULL,
    cpf VARCHAR(14),
    admission_date DATE,
    position VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, registration)
);

CREATE TABLE IF NOT EXISTS pontual.devices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES pontual.companies(id),
    serial_number VARCHAR(100) UNIQUE NOT NULL,
    model VARCHAR(100),
    firmware_version VARCHAR(50),
    communication_mode VARCHAR(20) NOT NULL CHECK (communication_mode IN ('MQTT', 'HTTP', 'SDK')),
    ip_address VARCHAR(45),
    location VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT true,
    active BOOLEAN NOT NULL DEFAULT true,
    last_seen TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pontual.attendance_events (
    id UUID DEFAULT uuid_generate_v4(),
    device_id UUID NOT NULL REFERENCES pontual.devices(id),
    employee_id UUID NOT NULL REFERENCES pontual.employees(id),
    event_type VARCHAR(20) NOT NULL CHECK (event_type IN ('CHECK_IN', 'CHECK_OUT', 'BREAK_START', 'BREAK_END', 'OTHER')),
    event_time TIMESTAMP WITH TIME ZONE NOT NULL,
    origin VARCHAR(10) NOT NULL CHECK (origin IN ('MQTT', 'HTTP', 'SDK')),
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Converter para hypertable (TimescaleDB)
SELECT create_hypertable('pontual.attendance_events', 'event_time', if_not_exists => TRUE);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_companies_document ON pontual.companies (document);
CREATE INDEX IF NOT EXISTS idx_api_users_company ON pontual.api_users (company_id);
CREATE INDEX IF NOT EXISTS idx_employees_company ON pontual.employees (company_id);
CREATE INDEX IF NOT EXISTS idx_employees_registration ON pontual.employees (company_id, registration);
CREATE INDEX IF NOT EXISTS idx_devices_company ON pontual.devices (company_id);
CREATE INDEX IF NOT EXISTS idx_devices_serial ON pontual.devices (serial_number);
CREATE INDEX IF NOT EXISTS idx_attendance_events_employee_time ON pontual.attendance_events (employee_id, event_time DESC);
CREATE INDEX IF NOT EXISTS idx_attendance_events_device_time ON pontual.attendance_events (device_id, event_time DESC);