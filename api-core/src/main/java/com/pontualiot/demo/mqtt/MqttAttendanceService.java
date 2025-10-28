package com.pontualiot.demo.mqtt;

// Importações para serviço MQTT
import com.pontualiot.demo.entity.Attendance; // Entidade de registro de ponto
import com.pontualiot.demo.entity.Employee;   // Entidade de funcionário
import com.pontualiot.demo.repository.AttendanceRepository; // Repositório de registros
import com.pontualiot.demo.repository.EmployeeRepository;   // Repositório de funcionários
import org.springframework.beans.factory.annotation.Autowired; // Injeção de dependência
import org.springframework.stereotype.Service; // Marca como serviço Spring

import java.time.LocalDate;     // Data local
import java.time.LocalDateTime; // Data e hora local
import java.util.List;          // Lista de registros
import java.util.Optional;      // Opcional para valores que podem não existir

/**
 * Serviço para processamento de mensagens MQTT de registro de ponto
 * 
 * Processa mensagens vindas de dispositivos IoT via MQTT
 * e converte em registros de ponto no banco de dados.
 */
@Service // Marca como serviço Spring gerenciado pelo container
public class MqttAttendanceService {

    // Injeção dos repositórios necessários
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private MqttMessageValidator messageValidator;

    /**
     * Processa mensagem MQTT de registro de ponto
     * 
     * Método principal que recebe payload MQTT, valida,
     * e processa o registro de entrada ou saída.
     * 
     * @param topic Tópico MQTT da mensagem
     * @param payload Payload JSON da mensagem
     * @return Registro de ponto criado ou atualizado
     * @throws IllegalArgumentException se mensagem ou dados inválidos
     */
    public Attendance processMqttMessage(String topic, String payload) {
        // Valida e faz parsing da mensagem MQTT
        MqttAttendanceMessage message = messageValidator.validateAndParse(payload);
        
        // Busca funcionário pelo RFID tag
        Employee employee = findEmployeeByRfid(message.getRfidTag());
        
        // Verifica se funcionário está ativo
        if (!employee.isActive()) {
            throw new IllegalArgumentException("Funcionário inativo: " + employee.getName());
        }
        
        // Processa baseado no tipo de evento
        if (message.isCheckIn()) {
            return processCheckIn(employee, message); // Processa entrada
        } else if (message.isCheckOut()) {
            return processCheckOut(employee, message); // Processa saída
        } else {
            throw new IllegalArgumentException("Tipo de evento inválido: " + message.getEventType());
        }
    }

    /**
     * Processa evento de entrada (check-in)
     * 
     * Cria novo registro de entrada, validando se
     * não há entrada duplicada no mesmo dia.
     * 
     * @param employee Funcionário que está fazendo check-in
     * @param message Mensagem MQTT com dados do evento
     * @return Registro de entrada criado
     * @throws IllegalArgumentException se já há entrada hoje
     */
    private Attendance processCheckIn(Employee employee, MqttAttendanceMessage message) {
        LocalDate eventDate = message.getTimestamp().toLocalDate(); // Extrai data do timestamp
        
        // Verifica se já existe entrada hoje
        List<Attendance> todayAttendances = attendanceRepository.findByEmployeeAndDate(employee, eventDate);
        
        // Procura por entrada existente sem saída
        for (Attendance attendance : todayAttendances) {
            if (attendance.getCheckIn() != null && attendance.getCheckOut() == null) {
                throw new IllegalArgumentException("Funcionário já registrou entrada hoje");
            }
        }
        
        // Cria novo registro de entrada
        Attendance attendance = Attendance.builder()
                .employee(employee)                    // Associa ao funcionário
                .checkIn(message.getTimestamp())       // Horário de entrada da mensagem
                .date(eventDate)                       // Data do evento
                .build();
        
        // Salva no banco de dados
        return attendanceRepository.save(attendance);
    }

    /**
     * Processa evento de saída (check-out)
     * 
     * Atualiza registro existente com horário de saída,
     * validando se há entrada prévia no dia.
     * 
     * @param employee Funcionário que está fazendo check-out
     * @param message Mensagem MQTT com dados do evento
     * @return Registro de saída atualizado
     * @throws IllegalArgumentException se não há entrada prévia
     */
    private Attendance processCheckOut(Employee employee, MqttAttendanceMessage message) {
        LocalDate eventDate = message.getTimestamp().toLocalDate(); // Extrai data do timestamp
        
        // Busca registros do funcionário na data
        List<Attendance> todayAttendances = attendanceRepository.findByEmployeeAndDate(employee, eventDate);
        
        // Procura registro em aberto (com entrada mas sem saída)
        Attendance openAttendance = null;
        for (Attendance attendance : todayAttendances) {
            if (attendance.getCheckIn() != null && attendance.getCheckOut() == null) {
                openAttendance = attendance; // Encontrou registro em aberto
                break;
            }
        }
        
        // Valida se existe entrada prévia
        if (openAttendance == null) {
            throw new IllegalArgumentException("Nenhum registro de entrada encontrado para hoje");
        }
        
        // Atualiza com horário de saída
        openAttendance.setCheckOut(message.getTimestamp());
        
        // Salva alteração no banco de dados
        return attendanceRepository.save(openAttendance);
    }

    /**
     * Busca funcionário por tag RFID
     * 
     * Método auxiliar para encontrar funcionário
     * baseado na tag RFID da mensagem MQTT.
     * 
     * @param rfidTag Tag RFID do funcionário
     * @return Funcionário encontrado
     * @throws IllegalArgumentException se RFID não encontrado
     */
    private Employee findEmployeeByRfid(String rfidTag) {
        // Busca funcionário no repositório
        Optional<Employee> employeeOpt = employeeRepository.findByRfidTag(rfidTag);
        
        // Verifica se funcionário foi encontrado
        if (employeeOpt.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado com RFID: " + rfidTag);
        }
        
        return employeeOpt.get(); // Retorna funcionário encontrado
    }
}