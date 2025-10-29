package com.pontualiot.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.eclipse.paho.client.mqttv3.*;
import org.junit.jupiter.api.*;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PontualIoTEndToEndTest {

    private static final String API_BASE_URL = "http://localhost:8080/api";
    private static final String MQTT_BROKER = "tcp://localhost:1883";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private static Long employeeId;
    private static String employeeRfidTag = "E2E_TEST_TAG_001";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = API_BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    private void waitForApi() {
        for (int i = 0; i < 15; i++) {
            try {
                given().when().get("/actuator/health").then().statusCode(200);
                return;
            } catch (Exception e) {
                try { Thread.sleep(2000); } catch (InterruptedException ie) {}
            }
        }
        throw new RuntimeException("API não disponível");
    }

    @Test
    @Order(1)
    @DisplayName("1. Sistema deve estar saudável")
    void shouldHaveHealthySystem() {
        waitForApi();
        given()
            .when()
                .get("/actuator/health")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    @Order(2)
    @DisplayName("2. Deve criar funcionário para testes")
    void shouldCreateEmployee() {
        waitForApi();
        Map<String, Object> employee = new HashMap<>();
        employee.put("name", "E2E Test Employee");
        employee.put("email", "e2e.test@pontualiot.com");
        employee.put("rfidTag", employeeRfidTag);
        employee.put("active", true);

        Response response = given()
            .contentType("application/json")
            .body(employee)
            .when()
                .post("/employees")
            .then()
                .statusCode(201)
                .extract().response();

        employeeId = response.jsonPath().getLong("id");
        assertNotNull(employeeId);
    }

    @Test
    @Order(3)
    @DisplayName("3. Deve processar evento MQTT de CHECK_IN")
    void shouldProcessMqttCheckInEvent() throws Exception {
        AtomicBoolean messagePublished = new AtomicBoolean(false);
        
        // Criar cliente MQTT
        MqttClient client = new MqttClient(MQTT_BROKER, MqttClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        
        try {
            client.connect(options);
            
            // Criar evento de CHECK_IN
            Map<String, Object> attendanceEvent = new HashMap<>();
            attendanceEvent.put("deviceId", "E2E_DEVICE_001");
            attendanceEvent.put("rfidTag", employeeRfidTag);
            attendanceEvent.put("eventType", "CHECK_IN");
            attendanceEvent.put("timestamp", System.currentTimeMillis());
            
            String payload = objectMapper.writeValueAsString(attendanceEvent);
            String topic = "attendance/E2E_DEVICE_001/CHECK_IN";
            
            // Publicar evento MQTT
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            client.publish(topic, message);
            messagePublished.set(true);
            
        } finally {
            if (client.isConnected()) {
                client.disconnect();
            }
        }
        
        assertTrue(messagePublished.get(), "MQTT message should be published");
        
        // Aguardar processamento do evento
        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                given()
                    .when()
                        .get("/attendances/employee/" + employeeId)
                    .then()
                        .statusCode(200)
                        .body("size()", greaterThan(0))
                        .body("[0].checkIn", notNullValue());
            });
    }

    @Test
    @Order(4)
    @DisplayName("4. Deve processar evento MQTT de CHECK_OUT")
    void shouldProcessMqttCheckOutEvent() throws Exception {
        AtomicBoolean messagePublished = new AtomicBoolean(false);
        
        MqttClient client = new MqttClient(MQTT_BROKER, MqttClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        
        try {
            client.connect(options);
            
            Map<String, Object> attendanceEvent = new HashMap<>();
            attendanceEvent.put("deviceId", "E2E_DEVICE_001");
            attendanceEvent.put("rfidTag", employeeRfidTag);
            attendanceEvent.put("eventType", "CHECK_OUT");
            attendanceEvent.put("timestamp", System.currentTimeMillis());
            
            String payload = objectMapper.writeValueAsString(attendanceEvent);
            String topic = "attendance/E2E_DEVICE_001/CHECK_OUT";
            
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            client.publish(topic, message);
            messagePublished.set(true);
            
        } finally {
            if (client.isConnected()) {
                client.disconnect();
            }
        }
        
        assertTrue(messagePublished.get(), "MQTT message should be published");
        
        // Aguardar processamento do CHECK_OUT
        Awaitility.await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofSeconds(1))
            .untilAsserted(() -> {
                given()
                    .when()
                        .get("/attendances/employee/" + employeeId)
                    .then()
                        .statusCode(200)
                        .body("[0].checkIn", notNullValue())
                        .body("[0].checkOut", notNullValue());
            });
    }

    @Test
    @Order(5)
    @DisplayName("5. Métricas devem refletir os eventos processados")
    void shouldHaveUpdatedMetrics() {
        String metricsResponse = given()
            .when()
                .get("/actuator/prometheus")
            .then()
                .statusCode(200)
                .extract().asString();

        assertTrue(metricsResponse.contains("attendance_records_total"), 
                  "Metrics should contain attendance_records_total");
        assertTrue(metricsResponse.contains("iot_devices_active"), 
                  "Metrics should contain iot_devices_active");
    }

    @Test
    @Order(6)
    @DisplayName("6. Prometheus deve estar coletando métricas")
    void shouldHavePrometheusMetrics() {
        given()
            .baseUri("http://localhost:9090")
            .when()
                .get("/api/v1/query?query=up")
            .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("data.result", not(empty()));
    }

    @Test
    @Order(7)
    @DisplayName("7. Grafana deve estar acessível")
    void shouldHaveGrafanaAccessible() {
        given()
            .baseUri("http://localhost:3000")
            .when()
                .get("/api/health")
            .then()
                .statusCode(200)
                .body("database", equalTo("ok"));
    }

    @AfterAll
    static void cleanup() {
        // Limpar dados de teste
        if (employeeId != null) {
            given()
                .when()
                    .delete("/employees/" + employeeId)
                .then()
                    .statusCode(anyOf(equalTo(200), equalTo(204), equalTo(404)));
        }
    }
}