import React from 'react';
import { render } from '@testing-library/react-native';
import App from '../App';

// TDD: Test 1 - App deve renderizar título
test('renders PontualIoT title', () => {
  const { getByText } = render(<App />);
  expect(getByText('PontualIoT')).toBeTruthy();
});

// TDD: Test 2 - App deve ter botões de check-in/out
test('renders check-in and check-out buttons', () => {
  const { getByText } = render(<App />);
  expect(getByText('Check-in')).toBeTruthy();
  expect(getByText('Check-out')).toBeTruthy();
});