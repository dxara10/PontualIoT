package com.pontualiot.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

class SystemIntegrationTest {

    private static final String API_URL = "http://localhost:8080/api";

    @BeforeAll
    static void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    private void waitForApi() {
        for (int i = 0; i < 15; i++) {
            try {
                given().baseUri(API_URL).when().get("/actuator/health").then().statusCode(200);
                return;
            } catch (Exception e) {
                try { Thread.sleep(2000); } catch (InterruptedException ie) {}
            }
        }
    }

    @Test
    @DisplayName("API Core deve estar rodando e saudável")
    void shouldHaveApiCoreRunning() {
        waitForApi();
        given()
            .baseUri(API_URL)
            .when()
                .get("/actuator/health")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    @DisplayName("Prometheus deve estar coletando métricas")
    void shouldHavePrometheusRunning() {
        given()
            .baseUri("http://localhost:9090")
            .when()
                .get("/api/v1/query?query=up")
            .then()
                .statusCode(200)
                .body("status", equalTo("success"));
    }

    @Test
    @DisplayName("Grafana deve estar acessível")
    void shouldHaveGrafanaRunning() {
        given()
            .baseUri("http://localhost:3000")
            .when()
                .get("/api/health")
            .then()
                .statusCode(200)
                .body("database", equalTo("ok"));
    }

    @Test
    @DisplayName("PostgreSQL deve estar conectado")
    void shouldHavePostgreSQLConnected() {
        waitForApi();
        given()
            .baseUri(API_URL)
            .when()
                .get("/actuator/health")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    @DisplayName("Métricas Prometheus devem estar expostas")
    void shouldExposePrometheusMetrics() {
        waitForApi();
        String response = given()
            .baseUri(API_URL)
            .when()
                .get("/actuator/prometheus")
            .then()
                .statusCode(200)
                .extract().asString();

        // Verificar métricas básicas
        assert response.contains("jvm_") : "Should contain JVM metrics";
    }
}