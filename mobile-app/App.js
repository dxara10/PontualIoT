import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert, ScrollView } from 'react-native';
import { attendanceService } from './src/services/attendanceService';

// Dados mockados para demonstração
const mockUser = {
  id: 1,
  name: 'João Silva',
  email: 'joao@pontualiot.com',
  rfidTag: 'RFID001'
};

const mockAttendances = [
  { id: 1, date: '2025-10-29', checkIn: '08:00', checkOut: '17:00', status: 'Completo' },
  { id: 2, date: '2025-10-28', checkIn: '08:15', checkOut: '17:30', status: 'Completo' },
  { id: 3, date: '2025-10-27', checkIn: '07:45', checkOut: null, status: 'Pendente' }
];

export default function App() {
  const [loading, setLoading] = useState(false);
  const [lastAction, setLastAction] = useState(null);

  const handleCheckIn = async () => {
    setLoading(true);
    try {
      // Simula sucesso sempre para demonstração
      setTimeout(() => {
        setLastAction({ type: 'CHECK_IN', time: new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) });
        Alert.alert('Sucesso', `Check-in realizado às ${new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}!`);
        setLoading(false);
      }, 1000);
    } catch (error) {
      Alert.alert('Erro', 'Falha no check-in');
      setLoading(false);
    }
  };

  const handleCheckOut = async () => {
    setLoading(true);
    try {
      // Simula sucesso sempre para demonstração
      setTimeout(() => {
        setLastAction({ type: 'CHECK_OUT', time: new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) });
        Alert.alert('Sucesso', `Check-out realizado às ${new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}!`);
        setLoading(false);
      }, 1000);
    } catch (error) {
      Alert.alert('Erro', 'Falha no check-out');
      setLoading(false);
    }
  };

  return (
    <ScrollView style={styles.scrollContainer}>
      <View style={styles.container}>
        <Text style={styles.title}>PontualIoT</Text>
        <Text style={styles.subtitle}>Sistema de Ponto Digital</Text>
        
        {/* Informações do Usuário */}
        <View style={styles.userCard}>
          <Text style={styles.userName}>{mockUser.name}</Text>
          <Text style={styles.userEmail}>{mockUser.email}</Text>
          <Text style={styles.userRfid}>RFID: {mockUser.rfidTag}</Text>
        </View>

        {/* Última Ação */}
        {lastAction && (
          <View style={styles.lastActionCard}>
            <Text style={styles.lastActionTitle}>Última Ação:</Text>
            <Text style={styles.lastActionText}>
              {lastAction.type === 'CHECK_IN' ? 'Entrada' : 'Saída'} às {lastAction.time}
            </Text>
          </View>
        )}
        
        {/* Botões de Ação */}
        <View style={styles.buttonContainer}>
          <TouchableOpacity 
            style={[styles.button, styles.checkInButton]} 
            onPress={handleCheckIn}
            disabled={loading}
          >
            <Text style={styles.buttonText}>
              {loading ? 'Processando...' : 'Check-in'}
            </Text>
          </TouchableOpacity>
          
          <TouchableOpacity 
            style={[styles.button, styles.checkOutButton]} 
            onPress={handleCheckOut}
            disabled={loading}
          >
            <Text style={styles.buttonText}>
              {loading ? 'Processando...' : 'Check-out'}
            </Text>
          </TouchableOpacity>
        </View>

        {/* Histórico Recente */}
        <View style={styles.historyContainer}>
          <Text style={styles.historyTitle}>Histórico Recente</Text>
          {mockAttendances.map((attendance) => (
            <View key={attendance.id} style={styles.historyItem}>
              <Text style={styles.historyDate}>{attendance.date}</Text>
              <Text style={styles.historyTime}>
                Entrada: {attendance.checkIn} | Saída: {attendance.checkOut || 'Pendente'}
              </Text>
              <Text style={[styles.historyStatus, 
                attendance.status === 'Completo' ? styles.statusComplete : styles.statusPending
              ]}>
                {attendance.status}
              </Text>
            </View>
          ))}
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  scrollContainer: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  container: {
    padding: 20,
    alignItems: 'center',
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#1890ff',
    marginBottom: 10,
    marginTop: 40,
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
    marginBottom: 30,
  },
  userCard: {
    backgroundColor: '#fff',
    padding: 20,
    borderRadius: 10,
    width: '100%',
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  userName: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 5,
  },
  userEmail: {
    fontSize: 14,
    color: '#666',
    marginBottom: 5,
  },
  userRfid: {
    fontSize: 12,
    color: '#999',
  },
  lastActionCard: {
    backgroundColor: '#e6f7ff',
    padding: 15,
    borderRadius: 8,
    width: '100%',
    marginBottom: 20,
    borderLeftWidth: 4,
    borderLeftColor: '#1890ff',
  },
  lastActionTitle: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#1890ff',
    marginBottom: 5,
  },
  lastActionText: {
    fontSize: 16,
    color: '#333',
  },
  buttonContainer: {
    width: '100%',
    gap: 15,
    marginBottom: 30,
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
  historyContainer: {
    width: '100%',
  },
  historyTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 15,
  },
  historyItem: {
    backgroundColor: '#fff',
    padding: 15,
    borderRadius: 8,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  historyDate: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 5,
  },
  historyTime: {
    fontSize: 14,
    color: '#666',
    marginBottom: 5,
  },
  historyStatus: {
    fontSize: 12,
    fontWeight: 'bold',
    textAlign: 'right',
  },
  statusComplete: {
    color: '#52c41a',
  },
  statusPending: {
    color: '#faad14',
  },
});