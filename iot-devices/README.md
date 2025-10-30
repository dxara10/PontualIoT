# 🔌 Dispositivos IoT Compatíveis - PontualIoT

## 📱 **Dispositivos com SDK Suportados**

### 🤖 **Android Things / Android**
- **Raspberry Pi 3/4** com Android Things
- **Tablets Android** industriais
- **Interface**: Touchscreen nativa
- **Integração**: App Android dedicado

### 🍓 **Raspberry Pi (Linux)**
- **Raspberry Pi 3/4/5** com Raspbian
- **Interface**: Display LCD + botões físicos
- **Linguagem**: Python com bibliotecas MQTT
- **Custo**: ~$50-80

### 🔧 **ESP32/ESP8266 (Arduino)**
- **ESP32 DevKit** com display OLED
- **Interface**: Display + botões tácteis
- **Linguagem**: C++ (Arduino IDE)
- **Custo**: ~$10-20

### 💻 **Mini PCs**
- **Intel NUC** / **ASUS Tinker Board**
- **Interface**: Monitor touchscreen
- **OS**: Linux/Windows
- **Linguagem**: Python/Node.js

### 📟 **Dispositivos Industriais**
- **Siemens IoT2000** series
- **Advantech IoT gateways**
- **Interface**: Display industrial + teclado
- **Protocolos**: MQTT, Modbus, OPC-UA

## 🖥️ **Interfaces Disponíveis**

### 📱 **Interface Touchscreen**
```
┌─────────────────────────┐
│     PontualIoT v1.0     │
├─────────────────────────┤
│  👤 Aproxime seu cartão │
│                         │
│  🏷️  [RFID: ____]       │
│                         │
│  ✅ Check-in            │
│  ❌ Check-out           │
│                         │
│  📊 Status: Online      │
└─────────────────────────┘
```

### 🔘 **Interface com Botões**
```
┌─────────────────────────┐
│  LCD 16x2 ou OLED      │
│  PontualIoT Ready       │
│  RFID: Aguardando...    │
└─────────────────────────┘
   [IN]  [OUT]  [MENU]
```

### 💡 **Interface LED + Buzzer**
```
🔴 LED Vermelho: Erro
🟡 LED Amarelo: Processando  
🟢 LED Verde: Sucesso
🔊 Buzzer: Feedback sonoro
```

## 🔗 **Integração com PontualIoT**

### 📡 **Protocolo MQTT**
```json
{
  "topic": "pontualiot/attendance",
  "payload": {
    "rfidTag": "RFID001",
    "action": "CHECK_IN",
    "timestamp": "2025-10-29T08:00:00Z",
    "deviceId": "DEVICE_001",
    "location": "Entrada Principal"
  }
}
```

### 🔌 **SDK de Integração**

**Python (Raspberry Pi):**
```python
import paho.mqtt.client as mqtt
import json
from datetime import datetime

class PontualIoTDevice:
    def __init__(self, device_id, mqtt_broker="localhost"):
        self.device_id = device_id
        self.client = mqtt.Client()
        self.client.connect(mqtt_broker, 1883, 60)
    
    def send_attendance(self, rfid_tag, action):
        payload = {
            "rfidTag": rfid_tag,
            "action": action,
            "timestamp": datetime.now().isoformat(),
            "deviceId": self.device_id
        }
        self.client.publish("pontualiot/attendance", json.dumps(payload))
```

**Arduino (ESP32):**
```cpp
#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>

class PontualIoTDevice {
  private:
    WiFiClient wifiClient;
    PubSubClient mqttClient;
    String deviceId;
  
  public:
    void sendAttendance(String rfidTag, String action) {
      StaticJsonDocument<200> doc;
      doc["rfidTag"] = rfidTag;
      doc["action"] = action;
      doc["timestamp"] = getTimestamp();
      doc["deviceId"] = deviceId;
      
      String payload;
      serializeJson(doc, payload);
      mqttClient.publish("pontualiot/attendance", payload.c_str());
    }
};
```

## 🏭 **Cenários de Implementação**

### 🏢 **Escritório Corporativo**
- **Dispositivo**: Tablet Android + leitor RFID
- **Interface**: App touchscreen personalizado
- **Localização**: Recepção/entrada

### 🏭 **Ambiente Industrial**
- **Dispositivo**: Raspberry Pi + display industrial
- **Interface**: Botões físicos resistentes
- **Proteção**: Case IP65 à prova d'água

### 🏪 **Pequeno Comércio**
- **Dispositivo**: ESP32 + OLED + buzzer
- **Interface**: Display simples + LEDs
- **Custo**: Solução econômica

### 🚚 **Veículos/Móvel**
- **Dispositivo**: Smartphone Android
- **Interface**: App móvel dedicado
- **Conectividade**: 4G/WiFi

## 📋 **Especificações Técnicas**

### 🔧 **Requisitos Mínimos**
- **Conectividade**: WiFi ou Ethernet
- **Protocolo**: MQTT 3.1.1
- **Leitor**: RFID 125kHz ou 13.56MHz
- **Memória**: 512MB RAM mínimo
- **Armazenamento**: 4GB para cache local

### ⚡ **Funcionalidades Avançadas**
- **Cache offline**: Armazena registros sem internet
- **Sincronização**: Upload automático quando online
- **Validação local**: Verifica RFIDs conhecidos
- **Logs**: Registro de todas as operações
- **OTA Updates**: Atualizações remotas

### 🔒 **Segurança**
- **Criptografia**: TLS/SSL para MQTT
- **Autenticação**: Certificados por dispositivo
- **Validação**: Assinatura digital dos dados
- **Backup**: Dados locais criptografados

## 💰 **Custos Estimados**

| Dispositivo | Custo | Interface | Adequado para |
|-------------|-------|-----------|---------------|
| **ESP32 + OLED** | $15-25 | Básica | Pequenas empresas |
| **Raspberry Pi** | $60-100 | Média | Escritórios |
| **Tablet Android** | $150-300 | Avançada | Corporações |
| **Industrial IoT** | $300-800 | Profissional | Indústrias |

**🎯 Nossa solução é flexível e pode rodar em qualquer dispositivo com conectividade e capacidade de executar código MQTT!**