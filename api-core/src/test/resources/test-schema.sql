-- Test schema (H2 compatible)
-- No TimescaleDB extensions for tests

-- Tabelas principais (sem schema específico para H2)
CREATE TABLE IF NOT EXISTS companies (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(255) NOT NULL,
    document VARCHAR(20) UNIQUE NOT NULL,
    address TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS api_users (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    company_id UUID NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'OPERATOR', 'VIEWER')),
    active BOOLEAN NOT NULL DEFAULT true,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE IF NOT EXISTS employees (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    company_id UUID NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    registration VARCHAR(50) NOT NULL,
    cpf VARCHAR(14),
    admission_date DATE,
    position VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, registration),
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE IF NOT EXISTS devices (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    company_id UUID NOT NULL,
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
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE IF NOT EXISTS attendance_events (
    id UUID DEFAULT RANDOM_UUID(),
    device_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    event_type VARCHAR(20) NOT NULL CHECK (event_type IN ('CHECK_IN', 'CHECK_OUT', 'BREAK_START', 'BREAK_END', 'OTHER')),
    event_time TIMESTAMP WITH TIME ZONE NOT NULL,
    origin VARCHAR(10) NOT NULL CHECK (origin IN ('MQTT', 'HTTP', 'SDK')),
    metadata JSON,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_companies_document ON companies (document);
CREATE INDEX IF NOT EXISTS idx_api_users_company ON api_users (company_id);
CREATE INDEX IF NOT EXISTS idx_employees_company ON employees (company_id);
CREATE INDEX IF NOT EXISTS idx_employees_registration ON employees (company_id, registration);
CREATE INDEX IF NOT EXISTS idx_devices_company ON devices (company_id);
CREATE INDEX IF NOT EXISTS idx_devices_serial ON devices (serial_number);
CREATE INDEX IF NOT EXISTS idx_attendance_events_employee_time ON attendance_events (employee_id, event_time DESC);
CREATE INDEX IF NOT EXISTS idx_attendance_events_device_time ON attendance_events (device_id, event_time DESC);