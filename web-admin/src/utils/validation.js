export const validateEmployee = (employee) => {
  const errors = [];

  if (!employee.name || employee.name.trim() === '') {
    errors.push('Nome é obrigatório');
  }

  if (!employee.email || employee.email.trim() === '') {
    errors.push('Email é obrigatório');
  } else if (!isValidEmail(employee.email)) {
    errors.push('Email inválido');
  }

  if (!employee.rfidTag || employee.rfidTag.trim() === '') {
    errors.push('Tag RFID é obrigatória');
  }

  return {
    isValid: errors.length === 0,
    errors
  };
};

export const validateAttendance = (attendance) => {
  const errors = [];

  if (!attendance.employeeId) {
    errors.push('ID do funcionário é obrigatório');
  }

  if (!attendance.date) {
    errors.push('Data é obrigatória');
  }

  if (!attendance.checkIn) {
    errors.push('Horário de entrada é obrigatório');
  }

  return {
    isValid: errors.length === 0,
    errors
  };
};

export const formatTime = (isoString) => {
  if (!isoString) return '-';
  
  try {
    const date = new Date(isoString);
    return date.toLocaleTimeString('pt-BR', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  } catch (error) {
    return '-';
  }
};

export const formatDate = (isoString) => {
  if (!isoString) return '-';
  
  try {
    const date = new Date(isoString);
    return date.toLocaleDateString('pt-BR');
  } catch (error) {
    return '-';
  }
};

const isValidEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};