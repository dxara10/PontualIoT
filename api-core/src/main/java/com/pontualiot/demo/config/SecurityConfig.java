package com.pontualiot.demo.config;

// ========================================
// IMPORTAÇÕES SPRING SECURITY
// ========================================
import org.springframework.context.annotation.Bean;    // Anotação para beans Spring
import org.springframework.context.annotation.Configuration; // Classe de configuração
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Builder HTTP
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Habilita segurança
import org.springframework.security.web.SecurityFilterChain; // Cadeia de filtros

/**
 * ========================================
 * CONFIGURAÇÃO DE SEGURANÇA - DESENVOLVIMENTO
 * ========================================
 * 
 * RESPONSABILIDADES:
 * - Configurar políticas de segurança HTTP
 * - Desabilitar proteções desnecessárias para desenvolvimento
 * - Permitir acesso livre aos endpoints
 * - Preparar base para autenticação futura
 * 
 * CONFIGURAÇÃO ATUAL (DESENVOLVIMENTO):
 * - CSRF desabilitado (APIs REST não precisam)
 * - Frame options desabilitado (Swagger UI funciona)
 * - Todas as requisições permitidas (sem autenticação)
 * - Session management padrão
 * 
 * ROADMAP PARA PRODUÇÃO:
 * 1. Implementar autenticação JWT
 * 2. Configurar OAuth2 (Google/Microsoft)
 * 3. Definir roles e permissões
 * 4. Proteger endpoints sensíveis
 * 5. Habilitar HTTPS obrigatório
 * 
 * ENDPOINTS QUE PRECISARÃO PROTEÇÃO:
 * - POST/PUT/DELETE /api/employees (apenas admin)
 * - GET /api/reports/* (apenas gestores)
 * - GET /api/attendances/* (apenas próprio funcionário ou admin)
 * - /api/actuator/* (apenas admin)
 * 
 * ENDPOINTS PÚBLICOS:
 * - GET /api/actuator/health (monitoramento)
 * - POST /api/auth/login (autenticação)
 * - GET /swagger-ui/* (documentação)
 * 
 * SEGURANÇA MQTT:
 * - Dispositivos IoT usarão certificados
 * - Tópicos MQTT protegidos por ACL
 * - Validação de deviceId nos payloads
 * 
 * LOGS DE SEGURANÇA:
 * - Tentativas de acesso negado
 * - Logins e logouts
 * - Mudanças de permissões
 * - Atividades administrativas
 */
@Configuration // Spring: marca como classe de configuração
@EnableWebSecurity // Spring Security: habilita configuração customizada
public class SecurityConfig {

    /**
     * BEAN PRINCIPAL - CADEIA DE FILTROS DE SEGURANÇA
     * 
     * Define como todas as requisições HTTP serão processadas
     * em termos de segurança, autenticação e autorização.
     * 
     * ARQUITETURA SPRING SECURITY:
     * 1. Requisição HTTP chega
     * 2. Passa por cadeia de filtros (SecurityFilterChain)
     * 3. Cada filtro aplica uma regra (CSRF, auth, etc)
     * 4. Se aprovada: chega no controller
     * 5. Se rejeitada: retorna erro HTTP
     * 
     * FILTROS CONFIGURADOS:
     * - DisableEncodeUrlFilter: desabilita encoding de URL
     * - WebAsyncManagerIntegrationFilter: suporte async
     * - SecurityContextHolderFilter: contexto de segurança
     * - HeaderWriterFilter: headers de segurança
     * - LogoutFilter: processamento de logout
     * - AnonymousAuthenticationFilter: usuários anônimos
     * - ExceptionTranslationFilter: tratamento de exceções
     * - AuthorizationFilter: autorização final
     * 
     * CONFIGURAÇÃO ATUAL:
     * - Modo permissivo (desenvolvimento)
     * - Sem autenticação obrigatória
     * - CSRF desabilitado
     * - Frame options desabilitado
     * 
     * @param http Builder para configuração HTTP Security
     * @return SecurityFilterChain configurada e pronta para uso
     * @throws Exception se houver erro na configuração
     */
    @Bean // Spring: registra como bean no contexto
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Log de início da configuração
        System.out.println("[SECURITY] Configurando segurança permissiva...");
        
        http
            // CONFIGURAÇÃO CSRF (Cross-Site Request Forgery)
            .csrf(csrf -> {
                System.out.println("[SECURITY] CSRF desabilitado");
                // Desabilita CSRF para APIs REST
                // APIs REST são stateless e não precisam de proteção CSRF
                // Permite POST/PUT/DELETE sem token CSRF
                csrf.disable();
            })
            
            // CONFIGURAÇÃO DE HEADERS HTTP
            .headers(headers -> {
                System.out.println("[SECURITY] Frame options desabilitado");
                // Desabilita X-Frame-Options
                // Permite que Swagger UI seja exibido em iframes
                // Necessário para documentação interativa
                headers.frameOptions().disable();
            })
            
            // CONFIGURAÇÃO DE AUTORIZAÇÃO
            .authorizeHttpRequests(auth -> {
                System.out.println("[SECURITY] Permitindo todas as requisições");
                
                // MODO DESENVOLVIMENTO: permite tudo
                auth.anyRequest().permitAll();
                
                // FUTURO - MODO PRODUÇÃO:
                // auth.requestMatchers("/api/actuator/health").permitAll()
                //     .requestMatchers("/api/auth/**").permitAll()
                //     .requestMatchers("/swagger-ui/**").permitAll()
                //     .requestMatchers(HttpMethod.GET, "/api/employees").hasRole("ADMIN")
                //     .requestMatchers(HttpMethod.POST, "/api/employees").hasRole("ADMIN")
                //     .requestMatchers("/api/attendances/**").hasAnyRole("USER", "ADMIN")
                //     .anyRequest().authenticated();
            });
        
        // Log de conclusão
        System.out.println("[SECURITY] ✅ Configuração de segurança aplicada");
        
        // Constrói e retorna a cadeia de filtros configurada
        return http.build();
    }
    
    // FUTURO - BEANS PARA AUTENTICAÇÃO:
    
    /*
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }
    
    @Bean
    public JwtRequestFilter jwtRequestFilter() {
        return new JwtRequestFilter();
    }
    
    @Bean
    public OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler() {
        return new OAuth2LoginSuccessHandler();
    }
    */
}