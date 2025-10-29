import { validateEmployee, validateAttendance, formatTime } from './validation';

describe('Validation Utils TDD', () => {
  describe('validateEmployee', () => {
    test('should validate correct employee data', () => {
      const validEmployee = {
        name: 'João Silva',
        email: 'joao@test.com',
        rfidTag: 'RFID001'
      };

      const result = validateEmployee(validEmployee);
      expect(result.isValid).toBe(true);
      expect(result.errors).toEqual([]);
    });

    test('should reject empty name', () => {
      const invalidEmployee = {
        name: '',
        email: 'joao@test.com',
        rfidTag: 'RFID001'
      };

      const result = validateEmployee(invalidEmployee);
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Nome é obrigatório');
    });

    test('should reject invalid email', () => {
      const invalidEmployee = {
        name: 'João Silva',
        email: 'invalid-email',
        rfidTag: 'RFID001'
      };

      const result = validateEmployee(invalidEmployee);
      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('Email inválido');
    });
  });

  describe('formatTime', () => {
    test('should format ISO string to HH:MM', () => {
      const isoString = '2024-01-15T08:30:00Z';
      const result = formatTime(isoString);
      expect(result).toMatch(/^\d{2}:\d{2}$/);
    });

    test('should handle null input', () => {
      const result = formatTime(null);
      expect(result).toBe('-');
    });
  });
});