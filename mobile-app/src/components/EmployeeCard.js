import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';

const EmployeeCard = ({ employee, onPress, showActions, onCheckIn, onCheckOut }) => {
  return (
    <TouchableOpacity
      testID="employee-card"
      style={styles.card}
      onPress={() => onPress && onPress(employee)}
    >
      <View style={styles.header}>
        <Text style={styles.name}>{employee.name}</Text>
        <Text style={[styles.status, employee.active ? styles.active : styles.inactive]}>
          {employee.active ? 'Ativo' : 'Inativo'}
        </Text>
      </View>
      
      <Text style={styles.email}>{employee.email}</Text>
      <Text style={styles.rfid}>RFID: {employee.rfidTag}</Text>
      
      {showActions && employee.active && (
        <View style={styles.actions}>
          <TouchableOpacity
            style={[styles.button, styles.checkInButton]}
            onPress={() => onCheckIn && onCheckIn(employee.id)}
          >
            <Text style={styles.buttonText}>Check-in</Text>
          </TouchableOpacity>
          
          <TouchableOpacity
            style={[styles.button, styles.checkOutButton]}
            onPress={() => onCheckOut && onCheckOut(employee.id)}
          >
            <Text style={styles.buttonText}>Check-out</Text>
          </TouchableOpacity>
        </View>
      )}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#fff',
    padding: 16,
    marginVertical: 8,
    marginHorizontal: 16,
    borderRadius: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  name: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  status: {
    fontSize: 12,
    fontWeight: '600',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  active: {
    backgroundColor: '#e8f5e8',
    color: '#2e7d32',
  },
  inactive: {
    backgroundColor: '#ffebee',
    color: '#c62828',
  },
  email: {
    fontSize: 14,
    color: '#666',
    marginBottom: 4,
  },
  rfid: {
    fontSize: 12,
    color: '#999',
    marginBottom: 12,
  },
  actions: {
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  button: {
    paddingHorizontal: 20,
    paddingVertical: 8,
    borderRadius: 6,
    minWidth: 80,
    alignItems: 'center',
  },
  checkInButton: {
    backgroundColor: '#4caf50',
  },
  checkOutButton: {
    backgroundColor: '#ff9800',
  },
  buttonText: {
    color: '#fff',
    fontWeight: '600',
    fontSize: 14,
  },
});

export default EmployeeCard;