import { renderHook, act, waitFor } from '@testing-library/react';
import useEmployees from './useEmployees';
import * as api from '../services/api';

jest.mock('../services/api');

describe('useEmployees Hook TDD', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should initialize with loading state', () => {
    api.getEmployees.mockReturnValue(new Promise(() => {}));
    
    const { result } = renderHook(() => useEmployees());
    
    expect(result.current.loading).toBe(true);
    expect(result.current.employees).toEqual([]);
    expect(result.current.error).toBe(null);
  });

  test('should load employees successfully', async () => {
    const mockEmployees = [
      { id: 1, name: 'João Silva', email: 'joao@test.com' },
      { id: 2, name: 'Maria Santos', email: 'maria@test.com' }
    ];
    
    api.getEmployees.mockResolvedValue(mockEmployees);
    
    const { result } = renderHook(() => useEmployees());
    
    await waitFor(() => {
      expect(result.current.loading).toBe(false);
      expect(result.current.employees).toEqual(mockEmployees);
      expect(result.current.error).toBe(null);
    });
  });

  test('should add employee successfully', async () => {
    const mockEmployees = [{ id: 1, name: 'João' }];
    const newEmployee = { id: 2, name: 'Maria', email: 'maria@test.com' };
    
    api.getEmployees.mockResolvedValue(mockEmployees);
    api.createEmployee.mockResolvedValue(newEmployee);
    
    const { result } = renderHook(() => useEmployees());
    
    await waitFor(() => {
      expect(result.current.employees).toEqual(mockEmployees);
    });
    
    await act(async () => {
      await result.current.addEmployee({ name: 'Maria', email: 'maria@test.com' });
    });
    
    expect(result.current.employees).toContainEqual(newEmployee);
  });
});