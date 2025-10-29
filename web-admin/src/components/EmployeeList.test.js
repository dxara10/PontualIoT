import { render, screen, waitFor } from '@testing-library/react';
import EmployeeList from './EmployeeList';

// Mock do serviço
jest.mock('../services/api', () => ({
  getEmployees: jest.fn(() => Promise.resolve([
    { id: 1, name: 'João Silva', email: 'joao@test.com', rfidTag: 'TAG001', active: true },
    { id: 2, name: 'Maria Santos', email: 'maria@test.com', rfidTag: 'TAG002', active: false }
  ]))
}));

// TDD: Test 1 - Lista deve renderizar funcionários
test('renders employee list', async () => {
  render(<EmployeeList />);
  
  await waitFor(() => {
    expect(screen.getByText('João Silva')).toBeInTheDocument();
    expect(screen.getByText('Maria Santos')).toBeInTheDocument();
  });
});

// TDD: Test 2 - Lista deve mostrar status ativo/inativo
test('shows employee status', async () => {
  render(<EmployeeList />);
  
  await waitFor(() => {
    expect(screen.getByText('Ativo')).toBeInTheDocument();
    expect(screen.getByText('Inativo')).toBeInTheDocument();
  });
});