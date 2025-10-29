import { render, screen } from '@testing-library/react';
import App from './App';

// TDD: Test 1 - App deve renderizar título
test('renders PontualIoT title', () => {
  render(<App />);
  const titleElement = screen.getByRole('heading', { name: /PontualIoT/i });
  expect(titleElement).toBeInTheDocument();
});

// TDD: Test 2 - App deve ter navegação
test('renders navigation menu', () => {
  render(<App />);
  const dashboardLink = screen.getByText(/Dashboard/i);
  const employeesLink = screen.getByText(/Funcionários/i);
  expect(dashboardLink).toBeInTheDocument();
  expect(employeesLink).toBeInTheDocument();
});