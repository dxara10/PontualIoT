import { useState } from 'react';
import AttendanceService from '../services/attendanceService';

const useAttendance = () => {
  const [employees, setEmployees] = useState([]);
  const [attendances, setAttendances] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const loadEmployees = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await AttendanceService.getEmployees();
      setEmployees(data);
    } catch (err) {
      setError('Erro ao carregar funcionários');
    } finally {
      setLoading(false);
    }
  };

  const loadAttendanceHistory = async (filters = {}) => {
    try {
      setLoading(true);
      setError(null);
      const data = await AttendanceService.getAttendanceHistory(filters);
      setAttendances(data);
    } catch (err) {
      setError('Erro ao carregar histórico');
    } finally {
      setLoading(false);
    }
  };

  const recordAttendance = async (employeeId, type) => {
    try {
      setError(null);
      const result = await AttendanceService.recordAttendance(employeeId, type);
      
      // Atualizar lista local se necessário
      if (attendances.length > 0) {
        setAttendances(prev => [result, ...prev]);
      }
      
      return result;
    } catch (err) {
      setError('Erro ao registrar ponto');
      throw err;
    }
  };

  const findEmployeeByRfid = async (rfidTag) => {
    try {
      setError(null);
      return await AttendanceService.getEmployeeByRfid(rfidTag);
    } catch (err) {
      setError('Erro ao buscar funcionário');
      throw err;
    }
  };

  return {
    employees,
    attendances,
    loading,
    error,
    loadEmployees,
    loadAttendanceHistory,
    recordAttendance,
    findEmployeeByRfid,
    clearError: () => setError(null)
  };
};

export default useAttendance;