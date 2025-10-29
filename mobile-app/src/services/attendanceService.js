import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 5000,
});

// Simulação de usuário logado (em produção viria do contexto/storage)
const CURRENT_EMPLOYEE_ID = 1;

class AttendanceService {
  async getEmployees() {
    try {
      const response = await api.get('/employees');
      return response.data;
    } catch (error) {
      console.error('Error fetching employees:', error);
      throw error;
    }
  }

  async recordAttendance(employeeId, type) {
    try {
      const response = await api.post('/attendances', {
        employeeId,
        type,
        timestamp: new Date().toISOString(),
      });
      return response.data;
    } catch (error) {
      if (error.response?.status === 404) {
        throw new Error('Employee not found');
      }
      console.error('Error recording attendance:', error);
      throw error;
    }
  }

  async getAttendanceHistory(filters = {}) {
    try {
      const response = await api.get('/attendances', { params: filters });
      return response.data;
    } catch (error) {
      console.error('Error fetching attendance history:', error);
      throw error;
    }
  }

  async getEmployeeByRfid(rfidTag) {
    try {
      const response = await api.get('/employees', { params: { rfidTag } });
      return response.data.length > 0 ? response.data[0] : null;
    } catch (error) {
      console.error('Error fetching employee by RFID:', error);
      throw error;
    }
  }

  // Legacy methods for backward compatibility
  async checkIn() {
    const response = await api.post(`/test-attendance/check-in/${CURRENT_EMPLOYEE_ID}`);
    return response.data;
  }

  async checkOut() {
    const response = await api.post(`/test-attendance/check-out/${CURRENT_EMPLOYEE_ID}`);
    return response.data;
  }

  async getAttendances() {
    const response = await api.get(`/attendances/employee/${CURRENT_EMPLOYEE_ID}`);
    return response.data;
  }
}

export const attendanceService = new AttendanceService();
export default new AttendanceService();