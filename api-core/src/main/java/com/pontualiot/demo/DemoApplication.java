package com.pontualiot.demo;

// Importações mínimas necessárias para aplicação Spring Boot
import org.springframework.boot.SpringApplication; // Classe principal para iniciar aplicação Spring Boot
import org.springframework.boot.autoconfigure.SpringBootApplication; // Anotação que habilita auto-configuração

/**
 * Classe principal da aplicação PontualIoT
 * 
 * Esta é a classe de entrada que inicializa toda a aplicação Spring Boot.
 * Mantida limpa e simples para focar no desenvolvimento TDD das funcionalidades.
 */
@SpringBootApplication // Habilita auto-configuração, component scan e configuração
public class DemoApplication {

    /**
     * Método main - ponto de entrada da aplicação
     * @param args argumentos da linha de comando
     */
    public static void main(String[] args) {
        // Inicia a aplicação Spring Boot
        SpringApplication.run(DemoApplication.class, args);
    }
}