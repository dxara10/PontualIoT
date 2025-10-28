-- H2 compatible schema for tests
CREATE TABLE IF NOT EXISTS companies (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    document VARCHAR(20) UNIQUE NOT NULL,
    address TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS employees (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    registration VARCHAR(50) NOT NULL,
    cpf VARCHAR(14),
    admission_date DATE,
    position VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, registration),
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE IF NOT EXISTS devices (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    serial_number VARCHAR(100) UNIQUE NOT NULL,
    model VARCHAR(100),
    firmware_version VARCHAR(50),
    communication_mode VARCHAR(20) NOT NULL CHECK (communication_mode IN ('MQTT', 'HTTP', 'SDK')),
    ip_address VARCHAR(45),
    location VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT true,
    active BOOLEAN NOT NULL DEFAULT true,
    last_seen TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE IF NOT EXISTS attendance_events (
    id UUID PRIMARY KEY,
    device_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    event_type VARCHAR(20) NOT NULL CHECK (event_type IN ('CHECK_IN', 'CHECK_OUT', 'BREAK_START', 'BREAK_END', 'OTHER')),
    event_time TIMESTAMP NOT NULL,
    origin VARCHAR(10) NOT NULL CHECK (origin IN ('MQTT', 'HTTP', 'SDK')),
    metadata VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);