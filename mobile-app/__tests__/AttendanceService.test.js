import AttendanceService from '../src/services/attendanceService';
import axios from 'axios';

// Mock axios
jest.mock('axios');
const mockedAxios = axios;

describe('AttendanceService TDD', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('getEmployees', () => {
    test('should fetch employees successfully', async () => {
      const mockEmployees = [
        { id: 1, name: 'João Silva', rfidTag: 'RFID001' },
        { id: 2, name: 'Maria Santos', rfidTag: 'RFID002' }
      ];

      mockedAxios.get.mockResolvedValueOnce({ data: mockEmployees });

      const result = await AttendanceService.getEmployees();

      expect(mockedAxios.get).toHaveBeenCalledWith('/employees');
      expect(result).toEqual(mockEmployees);
    });

    test('should handle fetch error', async () => {
      mockedAxios.get.mockRejectedValueOnce(new Error('Network error'));

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

      mockedAxios.post.mockResolvedValueOnce({ data: mockResponse });

      const result = await AttendanceService.recordAttendance(1, 'CHECK_IN');

      expect(mockedAxios.post).toHaveBeenCalledWith('/attendances', {
        employeeId: 1,
        type: 'CHECK_IN',
        timestamp: expect.any(String)
      });
      expect(result).toEqual(mockResponse);
    });

    test('should handle invalid employee ID', async () => {
      const error = new Error('Request failed');
      error.response = { status: 404 };
      mockedAxios.post.mockRejectedValueOnce(error);

      await expect(AttendanceService.recordAttendance(999, 'CHECK_IN'))
        .rejects.toThrow('Employee not found');
    });
  });

  describe('getAttendanceHistory', () => {
    test('should fetch attendance history with filters', async () => {
      const mockHistory = [
        { id: 1, employeeId: 1, checkIn: '08:00', checkOut: '17:00', date: '2024-01-15' }
      ];

      mockedAxios.get.mockResolvedValueOnce({ data: mockHistory });

      const filters = { employeeId: 1, startDate: '2024-01-01', endDate: '2024-01-31' };
      const result = await AttendanceService.getAttendanceHistory(filters);

      expect(mockedAxios.get).toHaveBeenCalledWith('/attendances', { params: filters });
      expect(result).toEqual(mockHistory);
    });
  });
});