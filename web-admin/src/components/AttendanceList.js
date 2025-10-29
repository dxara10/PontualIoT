import React, { useState, useEffect } from 'react';
import { getAttendances } from '../services/api';

const AttendanceList = () => {
  const [attendances, setAttendances] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchAttendances = async () => {
      try {
        setLoading(true);
        const data = await getAttendances();
        setAttendances(data);
      } catch (err) {
        setError('Erro ao carregar registros');
      } finally {
        setLoading(false);
      }
    };

    fetchAttendances();
  }, []);

  if (loading) return <div>Carregando registros...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div className="attendance-list">
      <h2>Registros de Ponto</h2>
      <table>
        <thead>
          <tr>
            <th>Funcionário</th>
            <th>Data</th>
            <th>Entrada</th>
            <th>Saída</th>
          </tr>
        </thead>
        <tbody>
          {attendances.map(attendance => (
            <tr key={attendance.id}>
              <td>{attendance.employee?.name}</td>
              <td>{attendance.date}</td>
              <td>{attendance.checkIn ? new Date(attendance.checkIn).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) : '-'}</td>
              <td>{attendance.checkOut ? new Date(attendance.checkOut).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) : '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default AttendanceList;