package com.pontualiot.demo.config;

// ========================================
// IMPORTAÇÕES SPRING SECURITY
// ========================================
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ========================================
 * CONFIGURAÇÃO DE SEGURANÇA
 * ========================================
 * 
 * OBJETIVO: Configurar segurança da API de forma permissiva para desenvolvimento
 * 
 * CONFIGURAÇÕES APLICADAS:
 * - CSRF desabilitado (para APIs REST)
 * - Frame options desabilitado (para Swagger UI)
 * - Todas as requisições permitidas (sem autenticação)
 * 
 * IMPORTANTE: Esta configuração é APENAS para desenvolvimento!
 * Em produção deve ter autenticação JWT adequada.
 */
@Configuration // Marca como classe de configuração Spring
@EnableWebSecurity // Habilita configuração customizada de segurança
public class SecurityConfig {

    /**
     * CONFIGURAÇÃO DA CADEIA DE FILTROS DE SEGURANÇA
     * 
     * Define como as requisições HTTP serão tratadas em termos de segurança.
     * Atualmente configurado para permitir tudo (desenvolvimento).
     * 
     * @param http objeto de configuração HTTP Security
     * @return SecurityFilterChain configurada
     * @throws Exception se houver erro na configuração
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("[SECURITY] Configurando segurança permissiva...");
        
        http
            // Desabilita CSRF (Cross-Site Request Forgery) - necessário para APIs REST
            .csrf(csrf -> {
                System.out.println("[SECURITY] CSRF desabilitado");
                csrf.disable();
            })
            // Desabilita frame options - permite Swagger UI funcionar
            .headers(headers -> {
                System.out.println("[SECURITY] Frame options desabilitado");
                headers.frameOptions().disable();
            })
            // Autorização: permite todas as requisições sem autenticação
            .authorizeHttpRequests(auth -> {
                System.out.println("[SECURITY] Permitindo todas as requisições");
                auth.anyRequest().permitAll();
            });
        
        System.out.println("[SECURITY] ✅ Configuração de segurança aplicada");
        return http.build();
    }
}