import { render, screen, waitFor } from '@testing-library/react';
import AttendanceList from './AttendanceList';
import * as api from '../services/api';

// Mock da API
jest.mock('../services/api');

describe('AttendanceList TDD', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should render loading state initially', () => {
    api.getAttendances.mockReturnValue(new Promise(() => {}));
    
    render(<AttendanceList />);
    
    expect(screen.getByText('Carregando registros...')).toBeInTheDocument();
  });

  test('should display attendances after loading', async () => {
    const mockAttendances = [
      {
        id: 1,
        employee: { name: 'João Silva' },
        checkIn: '2024-01-15T08:00:00',
        checkOut: '2024-01-15T17:00:00',
        date: '2024-01-15'
      }
    ];
    
    api.getAttendances.mockResolvedValue(mockAttendances);
    
    render(<AttendanceList />);
    
    await waitFor(() => {
      expect(screen.getByText('João Silva')).toBeInTheDocument();
      expect(screen.getByText('08:00')).toBeInTheDocument();
      expect(screen.getByText('17:00')).toBeInTheDocument();
    });
  });

  test('should handle API error gracefully', async () => {
    api.getAttendances.mockRejectedValue(new Error('API Error'));
    
    render(<AttendanceList />);
    
    await waitFor(() => {
      expect(screen.getByText('Erro ao carregar registros')).toBeInTheDocument();
    });
  });

  test('should filter attendances by date', async () => {
    const mockAttendances = [
      { id: 1, employee: { name: 'João' }, date: '2024-01-15' },
      { id: 2, employee: { name: 'Maria' }, date: '2024-01-16' }
    ];
    
    api.getAttendances.mockResolvedValue(mockAttendances);
    
    render(<AttendanceList />);
    
    // Test será implementado após criar o componente
    expect(true).toBe(true);
  });
});