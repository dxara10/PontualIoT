import AttendanceService from '../src/services/attendanceService';

// Mock fetch
global.fetch = jest.fn();

describe('AttendanceService TDD', () => {
  beforeEach(() => {
    fetch.mockClear();
  });

  describe('getEmployees', () => {
    test('should fetch employees successfully', async () => {
      const mockEmployees = [
        { id: 1, name: 'João Silva', rfidTag: 'RFID001' },
        { id: 2, name: 'Maria Santos', rfidTag: 'RFID002' }
      ];

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockEmployees,
      });

      const result = await AttendanceService.getEmployees();

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/employees');
      expect(result).toEqual(mockEmployees);
    });

    test('should handle fetch error', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      await expect(AttendanceService.getEmployees()).rejects.toThrow('Network error');
    });
  });

  describe('recordAttendance', () => {
    test('should record check-in successfully', async () => {
      const mockResponse = {
        id: 1,
        employeeId: 1,
        checkIn: '2024-01-15T08:00:00Z',
        type: 'CHECK_IN'
      };

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      });

      const result = await AttendanceService.recordAttendance(1, 'CHECK_IN');

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/attendances', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          employeeId: 1,
          type: 'CHECK_IN',
          timestamp: expect.any(String)
        }),
      });
      expect(result).toEqual(mockResponse);
    });

    test('should handle invalid employee ID', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 404,
        json: async () => ({ error: 'Employee not found' }),
      });

      await expect(AttendanceService.recordAttendance(999, 'CHECK_IN'))
        .rejects.toThrow('Employee not found');
    });
  });

  describe('getAttendanceHistory', () => {
    test('should fetch attendance history with filters', async () => {
      const mockHistory = [
        { id: 1, employeeId: 1, checkIn: '08:00', checkOut: '17:00', date: '2024-01-15' }
      ];

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockHistory,
      });

      const filters = { employeeId: 1, startDate: '2024-01-01', endDate: '2024-01-31' };
      const result = await AttendanceService.getAttendanceHistory(filters);

      expect(fetch).toHaveBeenCalledWith(
        'http://localhost:8080/api/attendances?employeeId=1&startDate=2024-01-01&endDate=2024-01-31'
      );
      expect(result).toEqual(mockHistory);
    });
  });
});