import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import EmployeeCard from '../../src/components/EmployeeCard';

describe('EmployeeCard TDD', () => {
  const mockEmployee = {
    id: 1,
    name: 'João Silva',
    email: 'joao@test.com',
    rfidTag: 'RFID001',
    active: true
  };

  test('should render employee information', () => {
    const { getByText } = render(
      <EmployeeCard employee={mockEmployee} />
    );

    expect(getByText('João Silva')).toBeTruthy();
    expect(getByText('joao@test.com')).toBeTruthy();
    expect(getByText(/RFID001/)).toBeTruthy();
  });

  test('should show active status', () => {
    const { getByText } = render(
      <EmployeeCard employee={mockEmployee} />
    );

    expect(getByText('Ativo')).toBeTruthy();
  });

  test('should call onPress when card is pressed', () => {
    const mockOnPress = jest.fn();
    
    const { getByTestId } = render(
      <EmployeeCard employee={mockEmployee} onPress={mockOnPress} />
    );

    fireEvent.press(getByTestId('employee-card'));
    expect(mockOnPress).toHaveBeenCalledWith(mockEmployee);
  });

  test('should call onCheckIn when check-in button is pressed', () => {
    const mockOnCheckIn = jest.fn();
    
    const { getByText } = render(
      <EmployeeCard 
        employee={mockEmployee} 
        showActions={true}
        onCheckIn={mockOnCheckIn}
      />
    );

    fireEvent.press(getByText('Check-in'));
    expect(mockOnCheckIn).toHaveBeenCalledWith(mockEmployee.id);
  });
});