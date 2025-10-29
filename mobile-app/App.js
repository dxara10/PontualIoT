import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import { attendanceService } from './src/services/attendanceService';

export default function App() {
  const [loading, setLoading] = useState(false);

  const handleCheckIn = async () => {
    setLoading(true);
    try {
      await attendanceService.checkIn();
      Alert.alert('Sucesso', 'Check-in realizado!');
    } catch (error) {
      Alert.alert('Erro', 'Falha no check-in');
    } finally {
      setLoading(false);
    }
  };

  const handleCheckOut = async () => {
    setLoading(true);
    try {
      await attendanceService.checkOut();
      Alert.alert('Sucesso', 'Check-out realizado!');
    } catch (error) {
      Alert.alert('Erro', 'Falha no check-out');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>PontualIoT</Text>
      <Text style={styles.subtitle}>Sistema de Ponto Digital</Text>
      
      <View style={styles.buttonContainer}>
        <TouchableOpacity 
          style={[styles.button, styles.checkInButton]} 
          onPress={handleCheckIn}
          disabled={loading}
        >
          <Text style={styles.buttonText}>Check-in</Text>
        </TouchableOpacity>
        
        <TouchableOpacity 
          style={[styles.button, styles.checkOutButton]} 
          onPress={handleCheckOut}
          disabled={loading}
        >
          <Text style={styles.buttonText}>Check-out</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
    padding: 20,
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#1890ff',
    marginBottom: 10,
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
    marginBottom: 50,
  },
  buttonContainer: {
    width: '100%',
    gap: 20,
  },
  button: {
    padding: 20,
    borderRadius: 10,
    alignItems: 'center',
  },
  checkInButton: {
    backgroundColor: '#52c41a',
  },
  checkOutButton: {
    backgroundColor: '#ff4d4f',
  },
  buttonText: {
    color: 'white',
    fontSize: 18,
    fontWeight: 'bold',
  },
});