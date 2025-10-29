// Mock do serviço
jest.mock('../services/api', () => ({
  getEmployees: jest.fn(() => Promise.resolve([]))
}));

// TDD: Test 1 - Mock deve estar funcionando
test('api mock is working', () => {
  const api = require('../services/api');
  expect(api.getEmployees).toBeDefined();
});

// TDD: Test 2 - Teste básico de importação
test('component can be imported', () => {
  const EmployeeList = require('./EmployeeList').default;
  expect(EmployeeList).toBeDefined();
});