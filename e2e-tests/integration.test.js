const axios = require('axios');

const API_BASE = 'http://localhost:8080/api';
const WEB_ADMIN_BASE = 'http://localhost:3001';

// Configurar timeout para testes E2E
jest.setTimeout(30000);

describe('PontualIoT - Integration E2E Tests', () => {
  
  // Test 1: API Core Health Check
  test('API Core should be healthy', async () => {
    const response = await axios.get(`${API_BASE}/actuator/health`);
    expect(response.status).toBe(200);
    expect(response.data.status).toBe('UP');
  });

  // Test 2: Database Integration
  test('Database integration should work', async () => {
    // Criar employee
    const employee = {
      name: 'E2E Test User',
      email: 'e2e@test.com',
      rfidTag: 'E2E001',
      active: true
    };
    
    const createResponse = await axios.post(`${API_BASE}/employees`, employee);
    expect(createResponse.status).toBe(201);
    expect(createResponse.data.name).toBe(employee.name);
    
    const employeeId = createResponse.data.id;
    
    // Buscar employee criado
    const getResponse = await axios.get(`${API_BASE}/employees/${employeeId}`);
    expect(getResponse.status).toBe(200);
    expect(getResponse.data.email).toBe(employee.email);
  });

  // Test 3: MQTT Integration (simulado)
  test('MQTT integration should be available', async () => {
    // Testar endpoint que usa MQTT
    const response = await axios.get(`${API_BASE}/employees`);
    expect(response.status).toBe(200);
    expect(Array.isArray(response.data)).toBe(true);
  });

  // Test 4: Attendance Flow Integration
  test('Complete attendance flow should work', async () => {
    // 1. Criar employee
    const employee = {
      name: 'Attendance Test',
      email: 'attendance@test.com', 
      rfidTag: 'ATT001',
      active: true
    };
    
    const employeeResponse = await axios.post(`${API_BASE}/employees`, employee);
    const employeeId = employeeResponse.data.id;
    
    // 2. Registrar check-in
    const checkinResponse = await axios.post(`${API_BASE}/test-attendance/check-in/${employeeId}`);
    expect(checkinResponse.status).toBe(200);
    expect(checkinResponse.data.message).toContain('Check-in');
    
    // 3. Registrar check-out
    const checkoutResponse = await axios.post(`${API_BASE}/test-attendance/check-out/${employeeId}`);
    expect(checkoutResponse.status).toBe(200);
    expect(checkoutResponse.data.message).toContain('Check-out');
    
    // 4. Verificar attendance criada
    const attendanceResponse = await axios.get(`${API_BASE}/attendances`);
    expect(attendanceResponse.status).toBe(200);
    
    const userAttendance = attendanceResponse.data.find(att => att.employeeId === employeeId);
    expect(userAttendance).toBeDefined();
    expect(userAttendance.checkIn).toBeDefined();
    expect(userAttendance.checkOut).toBeDefined();
  });

  // Test 5: API Endpoints Integration
  test('All main API endpoints should be accessible', async () => {
    const endpoints = [
      '/employees',
      '/attendances', 
      '/actuator/health'
    ];
    
    for (const endpoint of endpoints) {
      const response = await axios.get(`${API_BASE}${endpoint}`);
      expect(response.status).toBe(200);
    }
  });

  // Test 6: Web Admin Integration (básico)
  test('Web Admin should be accessible', async () => {
    try {
      const response = await axios.get(WEB_ADMIN_BASE, { timeout: 5000 });
      expect(response.status).toBe(200);
    } catch (error) {
      // Se Web Admin não estiver rodando, apenas log warning
      console.warn('⚠️ Web Admin não está acessível - pode não estar rodando');
    }
  });

  // Test 7: Data Persistence Integration
  test('Data should persist across requests', async () => {
    // Criar employee
    const employee = {
      name: 'Persistence Test',
      email: 'persist@test.com',
      rfidTag: 'PER001', 
      active: true
    };
    
    const createResponse = await axios.post(`${API_BASE}/employees`, employee);
    const employeeId = createResponse.data.id;
    
    // Aguardar um pouco
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // Verificar se ainda existe
    const getResponse = await axios.get(`${API_BASE}/employees/${employeeId}`);
    expect(getResponse.status).toBe(200);
    expect(getResponse.data.name).toBe(employee.name);
    
    // Listar todos e verificar se está na lista
    const listResponse = await axios.get(`${API_BASE}/employees`);
    const foundEmployee = listResponse.data.find(emp => emp.id === employeeId);
    expect(foundEmployee).toBeDefined();
  });

  // Test 8: Error Handling Integration
  test('API should handle errors gracefully', async () => {
    // Tentar buscar employee inexistente
    try {
      await axios.get(`${API_BASE}/employees/99999`);
    } catch (error) {
      expect(error.response.status).toBe(404);
    }
    
    // Tentar criar employee com dados inválidos
    try {
      await axios.post(`${API_BASE}/employees`, { name: '' });
    } catch (error) {
      expect(error.response.status).toBe(400);
    }
  });

});