#!/usr/bin/env python3
"""
PontualIoT - Simulador Raspberry Pi
Simula um dispositivo IoT de ponto eletrônico
"""

import json
import time
import random
import threading
from datetime import datetime
from dataclasses import dataclass
from typing import Optional

try:
    import paho.mqtt.client as mqtt
except ImportError:
    print("⚠️  Instale: pip install paho-mqtt")
    exit(1)

@dataclass
class AttendanceRecord:
    rfid_tag: str
    action: str
    timestamp: str
    device_id: str
    location: str = "Entrada Principal"

class PontualIoTDevice:
    def __init__(self, device_id: str, mqtt_broker: str = "localhost", mqtt_port: int = 1883):
        self.device_id = device_id
        self.mqtt_broker = mqtt_broker
        self.mqtt_port = mqtt_port
        self.client = mqtt.Client()
        self.is_connected = False
        self.setup_mqtt()
        
        # Simulação de funcionários conhecidos
        self.known_rfids = {
            "RFID001": "João Silva",
            "RFID002": "Maria Santos", 
            "RFID003": "Carlos Lima",
            "RFID004": "Ana Costa",
            "RFID005": "Pedro Oliveira"
        }
        
    def setup_mqtt(self):
        """Configura cliente MQTT"""
        self.client.on_connect = self.on_connect
        self.client.on_disconnect = self.on_disconnect
        self.client.on_message = self.on_message
        
    def on_connect(self, client, userdata, flags, rc):
        """Callback de conexão MQTT"""
        if rc == 0:
            self.is_connected = True
            print(f"✅ Dispositivo {self.device_id} conectado ao MQTT broker")
            client.subscribe("pontualiot/commands")
        else:
            print(f"❌ Falha na conexão MQTT: {rc}")
            
    def on_disconnect(self, client, userdata, rc):
        """Callback de desconexão MQTT"""
        self.is_connected = False
        print(f"⚠️  Dispositivo {self.device_id} desconectado do MQTT")
        
    def on_message(self, client, userdata, msg):
        """Processa comandos recebidos"""
        try:
            command = json.loads(msg.payload.decode())
            print(f"📨 Comando recebido: {command}")
        except Exception as e:
            print(f"❌ Erro ao processar comando: {e}")
    
    def connect(self):
        """Conecta ao broker MQTT"""
        try:
            self.client.connect(self.mqtt_broker, self.mqtt_port, 60)
            self.client.loop_start()
            return True
        except Exception as e:
            print(f"❌ Erro ao conectar: {e}")
            return False
    
    def send_attendance(self, rfid_tag: str, action: str) -> bool:
        """Envia registro de ponto via MQTT"""
        if not self.is_connected:
            print("❌ Dispositivo não conectado ao MQTT")
            return False
            
        record = AttendanceRecord(
            rfid_tag=rfid_tag,
            action=action,
            timestamp=datetime.now().isoformat(),
            device_id=self.device_id
        )
        
        payload = json.dumps({
            "rfidTag": record.rfid_tag,
            "action": record.action,
            "timestamp": record.timestamp,
            "deviceId": record.device_id,
            "location": record.location
        })
        
        try:
            result = self.client.publish("pontualiot/attendance", payload)
            if result.rc == 0:
                employee_name = self.known_rfids.get(rfid_tag, "Desconhecido")
                print(f"📤 {action}: {employee_name} ({rfid_tag}) às {record.timestamp[:19]}")
                return True
            else:
                print(f"❌ Falha ao enviar: {result.rc}")
                return False
        except Exception as e:
            print(f"❌ Erro ao enviar: {e}")
            return False
    
    def simulate_rfid_scan(self):
        """Simula leitura de cartão RFID"""
        rfid_tags = list(self.known_rfids.keys())
        actions = ["CHECK_IN", "CHECK_OUT"]
        
        while True:
            try:
                # Simula intervalo entre leituras (5-15 segundos)
                time.sleep(random.randint(5, 15))
                
                if self.is_connected:
                    rfid = random.choice(rfid_tags)
                    action = random.choice(actions)
                    self.send_attendance(rfid, action)
                else:
                    print("⚠️  Aguardando conexão MQTT...")
                    
            except KeyboardInterrupt:
                print("\n🛑 Simulação interrompida pelo usuário")
                break
            except Exception as e:
                print(f"❌ Erro na simulação: {e}")
                time.sleep(5)
    
    def display_interface(self):
        """Simula interface do dispositivo"""
        print("\n" + "="*50)
        print("🔌 PONTUALIOT - DISPOSITIVO IoT SIMULADO")
        print("="*50)
        print(f"📱 Device ID: {self.device_id}")
        print(f"🌐 MQTT Broker: {self.mqtt_broker}:{self.mqtt_port}")
        print(f"📍 Localização: Entrada Principal")
        print("="*50)
        print("👥 FUNCIONÁRIOS CADASTRADOS:")
        for rfid, name in self.known_rfids.items():
            print(f"   🏷️  {rfid}: {name}")
        print("="*50)
        print("🔄 Status: Aguardando cartões RFID...")
        print("💡 Pressione Ctrl+C para parar")
        print("="*50)

def main():
    """Função principal"""
    device_id = f"DEVICE_{random.randint(100, 999)}"
    
    # Criar dispositivo IoT
    device = PontualIoTDevice(device_id)
    
    # Mostrar interface
    device.display_interface()
    
    # Conectar ao MQTT
    if device.connect():
        # Aguardar conexão
        time.sleep(2)
        
        # Iniciar simulação
        try:
            device.simulate_rfid_scan()
        except KeyboardInterrupt:
            print("\n🛑 Encerrando dispositivo...")
        finally:
            device.client.loop_stop()
            device.client.disconnect()
            print("✅ Dispositivo desconectado com sucesso")
    else:
        print("❌ Não foi possível conectar ao MQTT broker")

if __name__ == "__main__":
    main()