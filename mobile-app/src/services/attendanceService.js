import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 5000,
});

// Simulação de usuário logado (em produção viria do contexto/storage)
const CURRENT_EMPLOYEE_ID = 1;

export const attendanceService = {
  checkIn: async () => {
    const response = await api.post(`/test-attendance/check-in/${CURRENT_EMPLOYEE_ID}`);
    return response.data;
  },

  checkOut: async () => {
    const response = await api.post(`/test-attendance/check-out/${CURRENT_EMPLOYEE_ID}`);
    return response.data;
  },

  getAttendances: async () => {
    const response = await api.get(`/attendances/employee/${CURRENT_EMPLOYEE_ID}`);
    return response.data;
  },
};