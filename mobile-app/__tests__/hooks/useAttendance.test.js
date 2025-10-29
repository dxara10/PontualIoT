import { renderHook, act } from '@testing-library/react-hooks';
import useAttendance from '../../src/hooks/useAttendance';
import AttendanceService from '../../src/services/attendanceService';

jest.mock('../../src/services/attendanceService');

describe('useAttendance Hook TDD', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('should initialize with default state', () => {
    const { result } = renderHook(() => useAttendance());

    expect(result.current.loading).toBe(false);
    expect(result.current.employees).toEqual([]);
    expect(result.current.error).toBe(null);
  });

  test('should load employees successfully', async () => {
    const mockEmployees = [
      { id: 1, name: 'João Silva', rfidTag: 'RFID001' }
    ];

    AttendanceService.getEmployees.mockResolvedValue(mockEmployees);

    const { result, waitForNextUpdate } = renderHook(() => useAttendance());

    act(() => {
      result.current.loadEmployees();
    });

    expect(result.current.loading).toBe(true);

    await waitForNextUpdate();

    expect(result.current.loading).toBe(false);
    expect(result.current.employees).toEqual(mockEmployees);
  });

  test('should record attendance successfully', async () => {
    const mockResponse = { id: 1, employeeId: 1, type: 'CHECK_IN' };
    AttendanceService.recordAttendance.mockResolvedValue(mockResponse);

    const { result } = renderHook(() => useAttendance());

    await act(async () => {
      await result.current.recordAttendance(1, 'CHECK_IN');
    });

    expect(AttendanceService.recordAttendance).toHaveBeenCalledWith(1, 'CHECK_IN');
  });
});