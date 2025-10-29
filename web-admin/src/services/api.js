import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 5000,
});

export const getEmployees = async () => {
  const response = await api.get('/employees');
  return response.data;
};

export const createEmployee = async (employee) => {
  const response = await api.post('/employees', employee);
  return response.data;
};

export const updateEmployee = async (id, employee) => {
  const response = await api.put(`/employees/${id}`, employee);
  return response.data;
};

export const deleteEmployee = async (id) => {
  await api.delete(`/employees/${id}`);
};

export const getAttendances = async (filters = {}) => {
  const response = await api.get('/attendances', { params: filters });
  return response.data;
};

export const createAttendance = async (attendance) => {
  const response = await api.post('/attendances', attendance);
  return response.data;
};