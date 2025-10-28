# Casos de Uso - Smart Attendance Platform

## 🎯 Casos de Uso Principais

### UC01 - Registrar Empresa
**Ator**: Administrador do Sistema  
**Pré-condições**: Usuário autenticado com role ADMIN  
**Fluxo Principal**:
1. Admin acessa formulário de cadastro de empresa
2. Preenche dados: nome, CNPJ, endereço
3. Sistema valida CNPJ único
4. Sistema cria empresa e usuário admin da empresa
5. Sistema envia credenciais por email

**Regras de Negócio**:
- CNPJ deve ser único no sistema
- Nome da empresa é obrigatório
- Primeiro usuário criado é automaticamente ADMIN da empresa

### UC02 - Cadastrar Dispositivo
**Ator**: Administrador da Empresa  
**Pré-condições**: Usuário autenticado, empresa ativa  
**Fluxo Principal**:
1. Admin acessa lista de dispositivos
2. Clica em "Novo Dispositivo"
3. Preenche: serial, modelo, modo comunicação, localização
4. Sistema valida serial único
5. Sistema registra dispositivo como OFFLINE
6. Sistema gera credenciais MQTT (se aplicável)

**Regras de Negócio**:
- Serial number deve ser único globalmente
- Modo de comunicação define protocolo de integração
- Dispositivo inicia sempre como OFFLINE

### UC03 - Registrar Funcionário
**Ator**: Operador/Admin da Empresa  
**Pré-condições**: Usuário autenticado na empresa  
**Fluxo Principal**:
1. Usuário acessa cadastro de funcionários
2. Preenche: nome, matrícula, CPF, cargo
3. Sistema valida matrícula única na empresa
4. Sistema valida CPF (formato)
5. Sistema registra funcionário como ATIVO

**Regras de Negócio**:
- Matrícula deve ser única por empresa
- CPF é opcional mas deve ser válido se informado
- Funcionário pode ser desativado (soft delete)

### UC04 - Processar Evento de Ponto (MQTT)
**Ator**: Dispositivo IoT  
**Pré-condições**: Dispositivo registrado e ativo  
**Fluxo Principal**:
1. Dispositivo publica evento no tópico `devices/{id}/events`
2. Sistema valida payload JSON
3. Sistema identifica funcionário (biometria/cartão)
4. Sistema determina tipo de evento (CHECK_IN/OUT)
5. Sistema persiste evento com timestamp
6. Sistema atualiza status do dispositivo (lastSeen)
7. Sistema publica ACK no tópico `devices/{id}/ack`

**Regras de Negócio**:
- Eventos duplicados são ignorados (idempotência)
- Timestamp do dispositivo é preservado
- Metadados originais são mantidos para auditoria

### UC05 - Processar Evento de Ponto (HTTP)
**Ator**: Dispositivo/Sistema Externo  
**Pré-condições**: Dispositivo registrado, autenticação válida  
**Fluxo Principal**:
1. Sistema recebe POST /api/events
2. Sistema valida JWT/API Key
3. Sistema valida payload
4. Sistema processa igual ao fluxo MQTT
5. Sistema retorna 201 Created com ID do evento

### UC06 - Enviar Comando para Dispositivo
**Ator**: Administrador/Operador  
**Pré-condições**: Dispositivo online, usuário autorizado  
**Fluxo Principal**:
1. Usuário seleciona dispositivo
2. Escolhe tipo de comando (REBOOT, SYNC_TIME, etc.)
3. Preenche parâmetros (se necessário)
4. Sistema registra comando como PENDING
5. Sistema publica comando no tópico MQTT
6. Sistema aguarda ACK do dispositivo
7. Sistema atualiza status para ACKNOWLEDGED

**Fluxo Alternativo**:
- Se timeout: status = TIMEOUT
- Se erro: status = FAILED

### UC07 - Consultar Relatório de Ponto
**Ator**: RH/Gestor  
**Pré-condições**: Usuário autorizado  
**Fluxo Principal**:
1. Usuário acessa relatórios
2. Filtra por: funcionário, período, dispositivo
3. Sistema consulta eventos de ponto
4. Sistema calcula: horas trabalhadas, pausas, extras
5. Sistema exibe relatório paginado
6. Usuário pode exportar CSV/PDF

**Regras de Negócio**:
- Relatórios respeitam multitenancy
- Dados são agregados por dia/funcionário
- Exportação tem limite de registros

### UC08 - Monitorar Status dos Dispositivos
**Ator**: Operador  
**Pré-condições**: Usuário autenticado  
**Fluxo Principal**:
1. Usuário acessa dashboard
2. Sistema exibe dispositivos em tempo real
3. Sistema mostra: status, última comunicação, alertas
4. Sistema atualiza automaticamente (WebSocket)
5. Usuário pode filtrar por localização/status

**Regras de Negócio**:
- Dispositivo OFFLINE se lastSeen > 15 minutos
- Alertas são gerados automaticamente
- Dashboard atualiza a cada 30 segundos

## 🔄 Fluxos de Integração

### Fluxo MQTT
```
Device → MQTT Broker → Ingestion Service → Core Service → Database
                    ↓
              Command Service ← Web Interface ← User
```

### Fluxo HTTP
```
Device → API Gateway → Ingestion Service → Core Service → Database
```

## 🚨 Tratamento de Erros

### Cenários de Falha
- **Dispositivo offline**: Eventos ficam em buffer local
- **Falha de rede**: Retry automático com backoff
- **Payload inválido**: Log de erro + notificação
- **Funcionário não encontrado**: Evento rejeitado + alerta