/*
 * PontualIoT - ESP32 Device Simulator
 * Simula dispositivo IoT para controle de ponto
 * Compatible with Arduino IDE
 */

#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <SPI.h>
#include <MFRC522.h>

// Configurações WiFi
const char* ssid = "SUA_REDE_WIFI";
const char* password = "SUA_SENHA_WIFI";

// Configurações MQTT
const char* mqtt_server = "192.168.1.100";  // IP do seu servidor
const int mqtt_port = 1883;
const char* device_id = "ESP32_001";

// Pinos do RFID
#define RST_PIN         22
#define SS_PIN          21

// Pinos dos LEDs
#define LED_RED         2
#define LED_GREEN       4
#define LED_BLUE        5

// Pinos dos botões
#define BTN_CHECKIN     18
#define BTN_CHECKOUT    19

// Objetos
WiFiClient wifiClient;
PubSubClient mqttClient(wifiClient);
MFRC522 rfid(SS_PIN, RST_PIN);

// Variáveis globais
String lastRfidTag = "";
unsigned long lastScanTime = 0;
bool deviceOnline = false;

// Funcionários conhecidos (simulação)
struct Employee {
  String rfidTag;
  String name;
};

Employee knownEmployees[] = {
  {"04:52:F3:2A", "João Silva"},
  {"04:A1:B2:3C", "Maria Santos"},
  {"04:C4:D5:6E", "Carlos Lima"},
  {"04:E7:F8:9A", "Ana Costa"},
  {"04:1B:2C:3D", "Pedro Oliveira"}
};

void setup() {
  Serial.begin(115200);
  
  // Configurar pinos
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);
  pinMode(BTN_CHECKIN, INPUT_PULLUP);
  pinMode(BTN_CHECKOUT, INPUT_PULLUP);
  
  // Inicializar RFID
  SPI.begin();
  rfid.PCD_Init();
  
  // Conectar WiFi
  setupWiFi();
  
  // Configurar MQTT
  mqttClient.setServer(mqtt_server, mqtt_port);
  mqttClient.setCallback(onMqttMessage);
  
  // Conectar MQTT
  connectMQTT();
  
  // Mostrar informações do dispositivo
  displayDeviceInfo();
  
  // LED azul indica inicialização
  setLED("blue");
  delay(2000);
  setLED("off");
}

void loop() {
  // Manter conexão MQTT
  if (!mqttClient.connected()) {
    connectMQTT();
  }
  mqttClient.loop();
  
  // Verificar cartão RFID
  checkRFID();
  
  // Verificar botões
  checkButtons();
  
  // Heartbeat a cada 30 segundos
  static unsigned long lastHeartbeat = 0;
  if (millis() - lastHeartbeat > 30000) {
    sendHeartbeat();
    lastHeartbeat = millis();
  }
  
  delay(100);
}

void setupWiFi() {
  Serial.println("🔌 Conectando ao WiFi...");
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  
  Serial.println();
  Serial.println("✅ WiFi conectado!");
  Serial.print("📡 IP: ");
  Serial.println(WiFi.localIP());
}

void connectMQTT() {
  while (!mqttClient.connected()) {
    Serial.println("🔗 Conectando ao MQTT...");
    
    if (mqttClient.connect(device_id)) {
      Serial.println("✅ MQTT conectado!");
      mqttClient.subscribe("pontualiot/commands");
      deviceOnline = true;
      setLED("green");
      delay(1000);
      setLED("off");
    } else {
      Serial.print("❌ Falha MQTT, rc=");
      Serial.println(mqttClient.state());
      setLED("red");
      delay(2000);
      setLED("off");
      delay(3000);
    }
  }
}

void onMqttMessage(char* topic, byte* payload, unsigned int length) {
  String message = "";
  for (int i = 0; i < length; i++) {
    message += (char)payload[i];
  }
  
  Serial.println("📨 Comando recebido: " + message);
  
  // Processar comandos
  StaticJsonDocument<200> doc;
  deserializeJson(doc, message);
  
  String command = doc["command"];
  if (command == "reboot") {
    Serial.println("🔄 Reiniciando dispositivo...");
    ESP.restart();
  }
}

void checkRFID() {
  // Verificar se há cartão presente
  if (!rfid.PICC_IsNewCardPresent() || !rfid.PICC_ReadCardSerial()) {
    return;
  }
  
  // Ler UID do cartão
  String rfidTag = "";
  for (byte i = 0; i < rfid.uid.size; i++) {
    rfidTag += String(rfid.uid.uidByte[i] < 0x10 ? "0" : "");
    rfidTag += String(rfid.uid.uidByte[i], HEX);
    if (i < rfid.uid.size - 1) rfidTag += ":";
  }
  rfidTag.toUpperCase();
  
  // Evitar leituras duplicadas
  if (rfidTag == lastRfidTag && (millis() - lastScanTime) < 3000) {
    return;
  }
  
  lastRfidTag = rfidTag;
  lastScanTime = millis();
  
  // Verificar se é funcionário conhecido
  String employeeName = getEmployeeName(rfidTag);
  if (employeeName != "") {
    Serial.println("🏷️  RFID detectado: " + rfidTag + " (" + employeeName + ")");
    
    // Determinar ação (simulação baseada em horário)
    String action = (hour() < 12) ? "CHECK_IN" : "CHECK_OUT";
    
    // Enviar registro
    sendAttendance(rfidTag, action);
    
    // Feedback visual
    setLED("green");
    delay(2000);
    setLED("off");
  } else {
    Serial.println("❌ RFID não autorizado: " + rfidTag);
    setLED("red");
    delay(1000);
    setLED("off");
  }
  
  // Parar leitura do cartão
  rfid.PICC_HaltA();
  rfid.PCD_StopCrypto1();
}

void checkButtons() {
  static bool lastCheckinState = HIGH;
  static bool lastCheckoutState = HIGH;
  
  bool checkinState = digitalRead(BTN_CHECKIN);
  bool checkoutState = digitalRead(BTN_CHECKOUT);
  
  // Botão Check-in pressionado
  if (lastCheckinState == HIGH && checkinState == LOW) {
    Serial.println("🔘 Botão Check-in pressionado");
    if (lastRfidTag != "") {
      sendAttendance(lastRfidTag, "CHECK_IN");
    }
  }
  
  // Botão Check-out pressionado
  if (lastCheckoutState == HIGH && checkoutState == LOW) {
    Serial.println("🔘 Botão Check-out pressionado");
    if (lastRfidTag != "") {
      sendAttendance(lastRfidTag, "CHECK_OUT");
    }
  }
  
  lastCheckinState = checkinState;
  lastCheckoutState = checkoutState;
}

void sendAttendance(String rfidTag, String action) {
  if (!mqttClient.connected()) {
    Serial.println("❌ MQTT desconectado, não foi possível enviar");
    return;
  }
  
  // Criar payload JSON
  StaticJsonDocument<300> doc;
  doc["rfidTag"] = rfidTag;
  doc["action"] = action;
  doc["timestamp"] = getTimestamp();
  doc["deviceId"] = device_id;
  doc["location"] = "Entrada Principal";
  
  String payload;
  serializeJson(doc, payload);
  
  // Publicar no MQTT
  if (mqttClient.publish("pontualiot/attendance", payload.c_str())) {
    String employeeName = getEmployeeName(rfidTag);
    Serial.println("📤 " + action + ": " + employeeName + " (" + rfidTag + ")");
    
    // Feedback visual de sucesso
    setLED("green");
    delay(500);
    setLED("off");
  } else {
    Serial.println("❌ Falha ao enviar registro");
    setLED("red");
    delay(500);
    setLED("off");
  }
}

void sendHeartbeat() {
  if (!mqttClient.connected()) return;
  
  StaticJsonDocument<200> doc;
  doc["deviceId"] = device_id;
  doc["status"] = "online";
  doc["timestamp"] = getTimestamp();
  doc["freeHeap"] = ESP.getFreeHeap();
  doc["uptime"] = millis();
  
  String payload;
  serializeJson(doc, payload);
  
  mqttClient.publish("pontualiot/heartbeat", payload.c_str());
}

String getEmployeeName(String rfidTag) {
  for (int i = 0; i < sizeof(knownEmployees) / sizeof(knownEmployees[0]); i++) {
    if (knownEmployees[i].rfidTag == rfidTag) {
      return knownEmployees[i].name;
    }
  }
  return "";
}

String getTimestamp() {
  // Simulação de timestamp (em produção usar NTP)
  return "2025-10-29T" + String(hour()) + ":" + String(minute()) + ":" + String(second()) + "Z";
}

int hour() { return (millis() / 3600000) % 24; }
int minute() { return (millis() / 60000) % 60; }
int second() { return (millis() / 1000) % 60; }

void setLED(String color) {
  // Desligar todos os LEDs
  digitalWrite(LED_RED, LOW);
  digitalWrite(LED_GREEN, LOW);
  digitalWrite(LED_BLUE, LOW);
  
  // Ligar LED específico
  if (color == "red") digitalWrite(LED_RED, HIGH);
  else if (color == "green") digitalWrite(LED_GREEN, HIGH);
  else if (color == "blue") digitalWrite(LED_BLUE, HIGH);
}

void displayDeviceInfo() {
  Serial.println("\n" + String("=").substring(0, 50));
  Serial.println("🔌 PONTUALIOT - ESP32 DEVICE");
  Serial.println(String("=").substring(0, 50));
  Serial.println("📱 Device ID: " + String(device_id));
  Serial.println("🌐 MQTT Server: " + String(mqtt_server));
  Serial.println("📍 Location: Entrada Principal");
  Serial.println(String("=").substring(0, 50));
  Serial.println("👥 FUNCIONÁRIOS CADASTRADOS:");
  for (int i = 0; i < sizeof(knownEmployees) / sizeof(knownEmployees[0]); i++) {
    Serial.println("   🏷️  " + knownEmployees[i].rfidTag + ": " + knownEmployees[i].name);
  }
  Serial.println(String("=").substring(0, 50));
  Serial.println("🔄 Status: Aguardando cartões RFID...");
  Serial.println(String("=").substring(0, 50));
}