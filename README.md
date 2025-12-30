# Assistente Financeiro - Sistema de Gestão Financeira com IA

Sistema completo de gestão financeira pessoal com cadastro de usuários, contas bancárias, controle de receitas/despesas, metas de economia, autenticação JWT, integração com IA (OpenAI + Ollama) e observabilidade com Prometheus.

## Tecnologias Utilizadas

- **Java 25** (compatível com 21+)
- **Spring Boot 3.5.6** 
- **Spring Security** com JWT
- **Spring Data JPA** com H2 Database
- **Spring AI** para integração com IA (OpenAI + Ollama)
- **Prometheus + Micrometer** para métricas
- **Spring Boot Admin** para monitoramento
- **Swagger/OpenAPI** para documentação
- **Lombok** para redução de boilerplate
- **Gradle** para gerenciamento de dependências

## Configuração e Execução

### Executar Aplicação
```bash
./gradlew bootRun
```

### URLs Principais
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Dashboard de Métricas:** http://localhost:8080/api/monitoring/dashboard
- **Métricas Prometheus:** http://localhost:8080/actuator/prometheus
- **H2 Console:** http://localhost:8080/h2-console
- **Spring Boot Admin:** http://localhost:8080/admin

### Configuração do Ollama (IA)

O sistema suporta duas formas de usar o Ollama: **Ollama Cloud** (recomendado) ou **Ollama Local**.

#### Opção 1: Ollama Cloud (Recomendado)

Para usar o Ollama Cloud com API key:

1. **Configuração Automática (Recomendado):**
   
   Execute o script de configuração apropriado para seu sistema:
   
   **Windows:**
   ```cmd
   setup-ollama-env.bat
   ```
   
   **Linux/macOS:**
   ```bash
   source setup-ollama-env.sh
   ```
   
   O script configurará automaticamente todas as variáveis de ambiente necessárias de forma segura.

2. **Configuração Manual:**
   
   Se preferir configurar manualmente, defina as variáveis de ambiente:
   
   **Windows (PowerShell):**
   ```powershell
   $env:OLLAMA_API_KEY="2533c67150b246b29d0c485a70c91010.c9ZN_GZ4EvrPA1b8DLaljyGo"
   $env:OLLAMA_BASE_URL="https://api.ollama.com"
   $env:OLLAMA_MODEL="Ewerton_Virginio/assitentefinanceiro"
   ```
   
   **Windows (CMD):**
   ```cmd
   set OLLAMA_API_KEY=2533c67150b246b29d0c485a70c91010.c9ZN_GZ4EvrPA1b8DLaljyGo
   set OLLAMA_BASE_URL=https://api.ollama.com
   set OLLAMA_MODEL=Ewerton_Virginio/assitentefinanceiro
   ```
   
   **Linux/macOS:**
   ```bash
   export OLLAMA_API_KEY="2533c67150b246b29d0c485a70c91010.c9ZN_GZ4EvrPA1b8DLaljyGo"
   export OLLAMA_BASE_URL="https://api.ollama.com"
   export OLLAMA_MODEL="Ewerton_Virginio/assitentefinanceiro"
   ```

3. **Verificação:**
   
   Após configurar, execute a aplicação. A validação automática mostrará nos logs:
   - ✅ "Configuração do Ollama: VALIDADA" (se tudo estiver correto)
   - ⚠️ "Configuração do Ollama: ATENÇÃO NECESSÁRIA" (se houver problemas)

**Vantagens do Ollama Cloud:**
- Não requer instalação local
- Sem necessidade de gerenciar servidor
- Escalável e sempre disponível
- Modelos pré-configurados

#### Opção 2: Ollama Local

Para usar a funcionalidade de assistente financeiro com IA local:

1. **Instalar Ollama:**
   ```bash
   # Windows
   winget install Ollama.Ollama
   
   # macOS
   brew install ollama
   
   # Linux
   curl -fsSL https://ollama.com/install.sh | sh
   ```

2. **Iniciar Ollama:**
   ```bash
   ollama serve
   ```

3. **Configurar no `application.properties`:**
   ```properties
   # Config do Ollama Local
   ollama.base-url=http://localhost:11434
   ollama.model=${OLLAMA_MODEL:Ewerton_Virginio/assitentefinanceiro}
   spring.ai.ollama.base-url=${ollama.base-url}
   spring.ai.ollama.chat.enabled=true
   spring.ai.ollama.chat.options.model=${ollama.model}
   spring.ai.ollama.chat.options.temperature=0.7
   spring.ai.ollama.chat.options.top-p=0.9
   ```

4. **Baixar e configurar modelo personalizado:**
   ```bash
   # Baixar modelo base
   ollama pull llama3.2
   
   # Criar Modelfile personalizado
   echo "FROM llama3.2" >> Modelfile
   echo "SYSTEM You are a friendly assistant." >> Modelfile
   
   # Criar modelo personalizado
   ollama create -f Modelfile Ewerton_Virginio/assitentefinanceiro
   ```

5. **Verificar instalação:**
   ```bash
   ollama list
   ```

#### Modelo Personalizado

O sistema está configurado para usar o modelo personalizado `Ewerton_Virginio/assitentefinanceiro`, que é baseado no Llama 3.2 mas otimizado para assistência financeira.

**Configuração Automática (Apenas para Ollama Local):**

Execute um dos scripts de configuração:

**Windows:**
```bash
setup-ollama-model.bat
```

**Linux/macOS:**
```bash
chmod +x setup-ollama-model.sh
./setup-ollama-model.sh
```

**Configuração Manual (Apenas para Ollama Local):**

Se preferir configurar manualmente:

```bash
# 1. Baixar modelo base
ollama pull llama3.2

# 2. Criar Modelfile personalizado
echo "FROM llama3.2" >> Modelfile
echo "SYSTEM Você é um assistente financeiro especializado..." >> Modelfile

# 3. Criar modelo personalizado
ollama create -f Modelfile Ewerton_Virginio/assitentefinanceiro

# 4. Verificar instalação
ollama list
```

## Usuário Mestre para Testes

Para facilitar os testes, o sistema cria automaticamente um usuário mestre na inicialização:

### Credenciais do Usuário Mestre
- **Email:** `admin@teste.com`
- **Senha:** `123`
- **Role:** `ADMIN`
- **Nome:** `Administrador Master`

### Como Usar
1. Execute a aplicação com `./gradlew bootRun`
2. Faça login com as credenciais acima
3. Use o token JWT retornado para acessar endpoints protegidos
4. O usuário mestre tem permissões de administrador para todos os módulos

### Exemplo de Login com Usuário Mestre
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@teste.com",
    "senha": "123"
  }'
```

## Exemplos cURL e Postman

### 1. Registrar Usuário
```bash
curl -X POST http://localhost:8080/api/v1/auth/registrarUsuario \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "joao@exemplo.com", 
    "senha": "minhasenha123",
    "role": "USER"
  }'
```

### 2. Fazer Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@exemplo.com",
    "senha": "minhasenha123"
  }'
```

### 3. Cadastrar Conta Bancária (Requer Token)
```bash
curl -X POST http://localhost:8080/api/v1/contas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI" \
  -d '{
    "banco": "Banco do Brasil",
    "numeroAgencia": "1234",
    "numeroConta": "567890",
    "tipoConta": "Corrente",
    "responsavel": "João Silva"
  }'
```

### 4. Listar Contas (Requer Token)
```bash
curl -X GET http://localhost:8080/api/v1/contas \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI"
```

### 5. Buscar Conta por ID (Requer Token)
```bash
curl -X GET http://localhost:8080/api/v1/contas/1 \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI"
```

### 6. Atualizar Conta (Requer Token)
```bash
curl -X PUT http://localhost:8080/api/v1/contas/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI" \
  -d '{
    "banco": "Itaú",
    "numeroAgencia": "5678",
    "numeroConta": "123456",
    "tipoConta": "Poupança",
    "responsavel": "João Silva"
  }'
```

### 7. Deletar Conta (Requer Token)
```bash
curl -X DELETE http://localhost:8080/api/v1/contas/1 \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI"
```

### 8. Chat com IA (Requer Token)
```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI" \
  -d '{
    "message": "Olá, como você pode me ajudar?"
  }'
```

### 8.1. Assistente Financeiro - Gerar Plano de Ação (Requer Token)
```bash
curl -X GET http://localhost:8080/api/ai/assistente-financeiro/plano-acao/1 \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI"
```

### 8.2. Assistente Financeiro - Analisar Viabilidade (Requer Token)
```bash
curl -X POST http://localhost:8080/api/ai/assistente-financeiro/analisar-viabilidade \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI" \
  -d '{
    "nome": "Viagem para Europa",
    "valorMeta": 15000.00,
    "dataInicio": "2024-01-01",
    "dataFim": "2024-12-31",
    "contaId": 1,
    "tipoMeta": "VIAGEM"
  }'
```

### 8.3. Assistente Financeiro - Sugestões de Otimização (Requer Token)
```bash
curl -X GET http://localhost:8080/api/ai/assistente-financeiro/sugestoes-otimizacao/1 \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI"
```

### 8.4. Assistente Financeiro - Status (Requer Token)
```bash
curl -X GET http://localhost:8080/api/ai/assistente-financeiro/status \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI"
```

### 8.5. IA Dinâmica - Responder Qualquer Prompt (Requer Token)
```bash
curl -X POST http://localhost:8080/api/ai/dinamica/responder \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI" \
  -d '{
    "prompt": "Como posso economizar mais dinheiro?",
    "contaId": 1
  }'
```

### 8.6. IA Dinâmica - Prompt Simples (Requer Token)
```bash
curl -X POST http://localhost:8080/api/ai/dinamica/responder-simples \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI" \
  -d '{
    "prompt": "Qual é a melhor forma de investir meu dinheiro?"
  }'
```

### 8.7. IA Dinâmica - Manter Conversação (Requer Token)
```bash
curl -X POST http://localhost:8080/api/ai/dinamica/conversacao \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI" \
  -d '{
    "prompt": "E sobre investimentos em ações?",
    "historico": [
      "Como posso economizar mais dinheiro?",
      "Você mencionou investimentos, pode me explicar melhor?"
    ],
    "contaId": 1
  }'
```

### 8.8. IA Dinâmica - Teste Rápido (Requer Token)
```bash
curl -X POST "http://localhost:8080/api/ai/dinamica/teste?pergunta=Como%20criar%20um%20orçamento%20mensal?" \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI"
```

### 8.9. IA Dinâmica - Usando DTO (Requer Token)
```bash
curl -X POST http://localhost:8080/api/ai/dinamica/responder-dto \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI" \
  -d '{
    "prompt": "Preciso de ajuda para planejar minha aposentadoria",
    "contaId": 1,
    "historico": []
  }'
```

### 9. Registrar Movimentação Financeira (Requer Token)
```bash
curl -X POST http://localhost:8080/api/v1/movimentacoes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI" \
  -d '{
    "tipoMovimentacao": "RECEITA",
    "valor": 1500.00,
    "descricao": "Venda de produto",
    "categoria": "VENDAS",
    "dataMovimentacao": "2024-01-15",
    "fonteMovimentacao": "MANUAL",
    "observacoes": "Pagamento à vista",
    "contaId": 1
  }'
```

### 10. Listar Movimentações (Requer Token)
```bash
curl -X GET http://localhost:8080/api/v1/movimentacoes \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI"
```

### 11. Criar Meta de Economia (Requer Token)
```bash
curl -X POST http://localhost:8080/api/v1/metas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI" \
  -d '{
    "nome": "Reserva de Emergência",
    "descricao": "Economizar para emergências médicas",
    "tipoMeta": "RESERVA_EMERGENCIA",
    "valorMeta": 10000.00,
    "dataInicio": "2024-01-01",
    "dataFim": "2024-12-31",
    "observacoes": "Meta para emergências médicas",
    "contaId": 1
  }'
```

### 11.1. Criar Meta com Análise de IA (Requer Token)
```bash
curl -X POST http://localhost:8080/api/v1/metas/com-analise-ia \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI" \
  -d '{
    "nome": "Casa Própria",
    "descricao": "Entrada para compra de casa",
    "tipoMeta": "IMOVEL",
    "valorMeta": 50000.00,
    "dataInicio": "2024-01-01",
    "dataFim": "2025-12-31",
    "contaId": 1
  }'
```

### 11.2. Gerar Plano de Ação para Meta (Requer Token)
```bash
curl -X GET http://localhost:8080/api/v1/metas/1/plano-acao \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI"
```

### 11.3. Sugestões de Otimização por Conta (Requer Token)
```bash
curl -X GET http://localhost:8080/api/v1/metas/conta/1/sugestoes-otimizacao \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI"
```

### 12. Listar Metas (Requer Token)
```bash
curl -X GET http://localhost:8080/api/v1/metas \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI"
```

### 13. Pausar Meta (Requer Token)
```bash
curl -X POST http://localhost:8080/api/v1/metas/1/pausar \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI"
```

### 14. Reativar Meta (Requer Token)
```bash
curl -X POST http://localhost:8080/api/v1/metas/1/reativar \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI"
```

### 15. Atualizar Progresso da Meta (Requer Token)
```bash
curl -X PUT http://localhost:8080/api/v1/metas/1/progresso \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI" \
  -d '{
    "valorAdicionado": 500.00
  }'
```

### 16. Verificar Metas Vencidas (Requer Token)
```bash
curl -X POST http://localhost:8080/api/v1/metas/verificar-vencidas \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI"
```

### 17. Buscar Metas Vencidas por Conta (Requer Token)
```bash
curl -X GET http://localhost:8080/api/v1/metas/conta/1/vencidas \
  -H "Authorization: Bearer SEU_TOKEN_JWT_AQUI"
```

## Funcionalidades Implementadas

### Autenticação e Autorização
- **Registro de usuários** com validação de dados
- **Login com JWT** e geração de tokens seguros
- **Sistema de roles** (USER/ADMIN)
- **Validação de campos obrigatórios**
- **Tratamento de erros** com mensagens descritivas

### Cadastro de Contas Bancárias
- **CRUD completo** para contas bancárias
- **Validação de dados** e campos obrigatórios
- **Verificação de duplicatas** por número de conta
- **Logs detalhados** para auditoria e debugging
- **Tratamento de erros** robusto

### Gestão Financeira
- **Controle de Receitas e Despesas** com categorização detalhada
- **Múltiplas Fontes de Dados**: Manual, importação de arquivo, API bancária
- **Acompanhamento de Saldo** com histórico de movimentações
- **Relatórios Financeiros** para análise de fluxo de caixa
- **Metas de Economia** com acompanhamento automático de progresso
- **Categorização Inteligente** com 20+ categorias específicas
- **Rastreabilidade Completa** com auditoria de transações
- **Validações Robustas** para todos os campos obrigatórios
- **Cálculo Automático de Saldo** baseado em movimentações

### Metas de Economia
- **Criação e Gerenciamento** de metas personalizadas
- **Acompanhamento de Progresso** automático com percentual
- **Status Inteligente**: Ativa, Pausada, Concluída, Vencida, Cancelada
- **Pausar e Reativar** metas conforme necessário
- **Verificação Automática** de metas vencidas
- **Relatórios Detalhados** de performance
- **Flexibilidade de Datas** para metas existentes

### Integração com IA
- **Chat com OpenAI** para assistência financeira geral (opcional, comentado por padrão)
- **Assistente Financeiro Inteligente** com Ollama (Cloud ou Local)
- **IA Dinâmica** que responde a qualquer prompt do usuário
- **Autenticação via API Key** para Ollama Cloud
- **Análise de Viabilidade** de metas financeiras
- **Geração de Planos de Ação** personalizados
- **Sugestões de Otimização** para metas ativas
- **Consultor Automatizado** para estratégias de poupança e investimento
- **Recomendações Personalizadas** baseadas no perfil financeiro
- **Análise de Padrões** de gastos e receitas
- **Cálculo Automático** de economia mensal necessária
- **Comparação com Capacidade Atual** do usuário
- **Conversação Contextualizada** com memória de mensagens anteriores
- **Categorização Inteligente** de prompts por tipo de assunto
- **Respostas Especializadas** por área financeira (investimentos, orçamento, dívidas, etc.)

### Observabilidade
- **Métricas customizadas** com Prometheus
- **Dashboard de monitoramento** personalizado
- **Health checks** e status da aplicação
- **Logs estruturados** com SLF4J
- **Retornos HTTP Informativos** com detalhes de operações

### Qualidade de Código
- **Clean Code** e princípios SOLID
- **Tratamento de exceções** centralizado
- **Validação de dados** consistente
- **Logs informativos** para debugging
- **Documentação Swagger** completa
- **Testes Unitários** com cobertura completa (53 testes)
- **Imutabilidade** com construtores em vez de setters
- **Mensagens de Log** sem caracteres especiais para compatibilidade

## Observabilidade e Métricas

### Métricas Customizadas
- `adsacoma_login_attempts_total` - Tentativas de login
- `adsacoma_login_success_total` - Logins bem-sucedidos  
- `adsacoma_login_failures_total` - Falhas de login
- `adsacoma_user_registrations_total` - Registros de usuários

### Endpoints de Monitoramento
- **Dashboard de Métricas:** `/api/monitoring/dashboard`
- **Status da Aplicação:** `/api/monitoring/health`
- **Prometheus:** `/actuator/prometheus`
- **Health Check:** `/actuator/health`

### Exemplo - Dashboard de Métricas
```bash
curl http://localhost:8080/api/monitoring/dashboard
```

**Resposta:**
```json
{
  "sucesso": true,
  "mensagem": "Dashboard de métricas carregado com sucesso",
  "dados": {
    "status": "UP",
    "custom_metrics": {
      "login_attempts": 10,
      "login_success": 8,
      "user_registrations": 5
    },
    "system_metrics": {
      "memory_used_bytes": 134217728,
      "http_requests_total": 25
    },
    "timestamp": 1726677600000
  }
}
```

## Estrutura do Projeto

```
src/main/java/com/cadastro/adsacoma/
├── ai/                          # Integração com IA
│   ├── controller/              # Controllers de IA
│   │   ├── AssistenteFinanceiroController.java  # Assistente financeiro
│   │   └── IADinamicaController.java           # IA dinâmica
│   ├── service/                 # Serviços de IA
│   │   ├── AssistenteFinanceiroService.java    # Serviço do assistente
│   │   └── IADinamicaService.java              # Serviço IA dinâmica
│   └── domain/                  # DTOs de IA
│       └── dto/
│           └── PromptRequestDTO.java           # DTO para prompts
├── cadastro/                    # Módulo de cadastro de contas
│   ├── controller/              # Controllers de contas
│   ├── domain/                  # Entidades e DTOs
│   ├── service/                 # Serviços de contas
│   └── repository/              # Repositórios JPA
├── financeiro/                  # Módulo de gestão financeira
│   ├── domain/                  # Entidades e DTOs
│   │   ├── MovimentacaoFinanceira.java    # Movimentações financeiras
│   │   ├── MetaEconomia.java             # Metas de economia
│   │   ├── dto/                           # DTOs
│   │   └── enums/                         # Enums específicos
│   ├── controller/              # Controllers financeiros
│   ├── service/                 # Serviços financeiros
│   └── repository/              # Repositórios JPA
├── common/                      # Classes utilitárias
├── config/                      # Configurações
│   ├── SecurityConfig.java      # Configuração de segurança
│   ├── JwtRequestFilter.java    # Filtro JWT
│   ├── SwaggerConfig.java       # Configuração Swagger
│   └── OllamaConfig.java        # Configuração Ollama
├── login/                       # Módulo de autenticação
│   ├── controller/              # Controllers de auth
│   ├── domain/                  # Entidades e DTOs
│   ├── service/                 # Serviços de usuário
│   └── repository/              # Repositórios JPA
└── monitoring/                  # Módulo de monitoramento
    └── controller/              # Controllers de métricas

src/test/java/com/cadastro/adsacoma/
├── cadastro/                    # Testes do módulo de cadastro
│   ├── controller/              # Testes de controllers
│   ├── service/                 # Testes de services
│   └── service/testdata/        # Dados de teste (Builders)
└── financeiro/                  # Testes do módulo financeiro
    ├── service/                 # Testes de services
    └── service/testdata/        # Dados de teste (Builders)
```

# Configuração de Variáveis de Ambiente

Este documento descreve como configurar as variáveis de ambiente necessárias para o funcionamento do Assistente Financeiro.

## Variáveis Obrigatórias para Ollama Cloud

Se você estiver usando **Ollama Cloud**, configure as seguintes variáveis:

### Windows (PowerShell)
```powershell
$env:OLLAMA_API_KEY="sua_api_key_aqui"
$env:OLLAMA_BASE_URL="https://api.ollama.com"
$env:OLLAMA_MODEL="Ewerton_Virginio/assitentefinanceiro"
```

### Windows (CMD)
```cmd
set OLLAMA_API_KEY=sua_api_key_aqui
set OLLAMA_BASE_URL=https://api.ollama.com
set OLLAMA_MODEL=Ewerton_Virginio/assitentefinanceiro
```

### Linux/macOS
```bash
export OLLAMA_API_KEY="sua_api_key_aqui"
export OLLAMA_BASE_URL="https://api.ollama.com"
export OLLAMA_MODEL="Ewerton_Virginio/assitentefinanceiro"
```

## Variáveis para Ollama Local

Se você estiver usando **Ollama Local**, configure apenas:

### Windows (PowerShell)
```powershell
$env:OLLAMA_BASE_URL="http://localhost:11434"
$env:OLLAMA_MODEL="Ewerton_Virginio/assitentefinanceiro"
```

### Linux/macOS
```bash
export OLLAMA_BASE_URL="http://localhost:11434"
export OLLAMA_MODEL="Ewerton_Virginio/assitentefinanceiro"
```

## Configuração Permanente

### Windows
Crie um arquivo `.env` na raiz do projeto ou configure nas variáveis de ambiente do sistema:
1. Painel de Controle → Sistema → Configurações Avançadas do Sistema
2. Variáveis de Ambiente → Novo (usuário ou sistema)

### Linux/macOS
Adicione ao arquivo `~/.bashrc` ou `~/.zshrc`:
```bash
export OLLAMA_API_KEY="sua_api_key_aqui"
export OLLAMA_BASE_URL="https://api.ollama.com"
export OLLAMA_MODEL="Ewerton_Virginio/assitentefinanceiro"
```

Depois execute:
```bash
source ~/.bashrc  # ou source ~/.zshrc
```

## Variáveis Opcionais

### JWT (Já possui valores padrão)
```bash
JWT_SECRET=seu_secret_jwt_aqui  # Opcional
JWT_EXPIRATION=3600              # Opcional (padrão: 3600 segundos)
```

## Verificação

Após configurar as variáveis, execute a aplicação. Você verá nos logs:

- **Sucesso**: "Ollama Cloud configurado com sucesso" ou "Ollama Local configurado"
- **Aviso**: "OLLAMA_API_KEY não configurada!" (se usar Ollama Cloud sem API key)

## Segurança

**IMPORTANTE:**
- **NUNCA** commite arquivos `.env` no repositório
- **NUNCA** deixe API keys hardcoded no código
- Use variáveis de ambiente ou gerenciadores de secrets em produção
- O arquivo `.gitignore` já está configurado para ignorar arquivos `.env`

## Obtenção da API Key

1. Acesse https://ollama.com
2. Crie uma conta ou faça login
3. Acesse o dashboard de API keys
4. Gere uma nova API key
5. Copie e configure na variável de ambiente `OLLAMA_API_KEY`

## Autenticação

### Fluxo
1. Registre com nome, email, senha e role
2. Faça login com email e senha  
3. Use o token JWT nos headers: `Authorization: Bearer <token>`

### Roles
- **USER:** Usuário comum (padrão)
- **ADMIN:** Administrador (acesso total)

### Endpoints de Autenticação
- `POST /api/v1/auth/registrarUsuario` - Registrar usuário
- `POST /api/v1/auth/login` - Fazer login
- `PUT /api/v1/auth/atualizarUsuario` - Atualizar usuário (requer token)

### Endpoints de Contas Bancárias
- `GET /api/v1/contas` - Listar todas as contas
- `GET /api/v1/contas/{id}` - Buscar conta por ID
- `POST /api/v1/contas` - Criar nova conta
- `PUT /api/v1/contas/{id}` - Atualizar conta
- `DELETE /api/v1/contas/{id}` - Deletar conta

### Endpoints de Gestão Financeira
- `GET /api/v1/movimentacoes` - Listar movimentações financeiras
- `GET /api/v1/movimentacoes/{id}` - Buscar movimentação por ID
- `POST /api/v1/movimentacoes` - Registrar nova movimentação
- `PUT /api/v1/movimentacoes/{id}` - Atualizar movimentação
- `DELETE /api/v1/movimentacoes/{id}` - Deletar movimentação
- `GET /api/v1/movimentacoes/conta/{contaId}` - Movimentações por conta
- `GET /api/v1/movimentacoes/periodo` - Movimentações por período

### Endpoints de Metas de Economia
- `GET /api/v1/metas` - Listar metas de economia
- `GET /api/v1/metas/{id}` - Buscar meta por ID
- `POST /api/v1/metas` - Criar nova meta
- `POST /api/v1/metas/com-analise-ia` - Criar meta com análise de IA
- `PUT /api/v1/metas/{id}` - Atualizar meta
- `DELETE /api/v1/metas/{id}` - Deletar meta
- `GET /api/v1/metas/conta/{contaId}` - Metas por conta
- `GET /api/v1/metas/{id}/plano-acao` - Gerar plano de ação com IA
- `GET /api/v1/metas/conta/{contaId}/sugestoes-otimizacao` - Sugestões de otimização
- `PUT /api/v1/metas/{id}/progresso` - Atualizar progresso da meta
- `POST /api/v1/metas/{id}/pausar` - Pausar meta
- `POST /api/v1/metas/{id}/reativar` - Reativar meta
- `POST /api/v1/metas/verificar-vencidas` - Verificar metas vencidas
- `GET /api/v1/metas/conta/{contaId}/vencidas` - Metas vencidas por conta

### Endpoints do Assistente Financeiro IA
- `GET /api/ai/assistente-financeiro/plano-acao/{metaId}` - Gerar plano de ação
- `POST /api/ai/assistente-financeiro/analisar-viabilidade` - Analisar viabilidade
- `GET /api/ai/assistente-financeiro/sugestoes-otimizacao/{contaId}` - Sugestões de otimização
- `GET /api/ai/assistente-financeiro/status` - Status do assistente

### Endpoints da IA Dinâmica
- `POST /api/ai/dinamica/responder` - Responder qualquer prompt com contexto
- `POST /api/ai/dinamica/responder-simples` - Responder prompt simples
- `POST /api/ai/dinamica/conversacao` - Manter conversação com histórico
- `POST /api/ai/dinamica/teste` - Teste rápido da IA
- `POST /api/ai/dinamica/responder-dto` - Usando DTO estruturado
- `GET /api/ai/dinamica/status` - Status da IA dinâmica

## Regras de Negócio do Sistema Financeiro

### Objetivo Principal
O sistema tem como finalidade organizar a vida financeira do usuário e atuar como consultor automatizado, auxiliando em:

- **Controle de receitas e despesas** com categorização detalhada
- **Definição e acompanhamento de metas de economia** com progresso automático
- **Sugestão de estratégias de poupança e investimento** via IA
- **Recomendações personalizadas** baseadas no perfil financeiro do usuário

### Tipos de Movimentação
- **RECEITA**: Entrada de valores (salário, vendas, investimentos)
- **DESPESA**: Saída de valores (compras, serviços, utilidades)
- **TRANSFERENCIA**: Movimentação entre contas
- **INVESTIMENTO**: Aplicações financeiras

### Categorias Disponíveis
**Receitas:** SALARIO, VENDAS, FREELANCE, INVESTIMENTOS_RENDIMENTOS, EMPRESTIMOS_RECEBIDOS, OUTRAS_RECEITAS

**Despesas:** ALIMENTACAO, TRANSPORTE, MORADIA, SAUDE, EDUCACAO, LAZER, UTILIDADES, COMPRAS, SERVICOS, INVESTIMENTOS_APLICADOS, EMPRESTIMOS_PAGOS, OUTRAS_DESPESAS

**Transferências:** TRANSFERENCIA_ENTRE_CONTAS

**Investimentos:** POUPANCA, CDB, FUNDOS, ACOES, CRIPTOMOEDAS

### Fontes de Movimentação
- **MANUAL**: Registro manual pelo usuário
- **IMPORTACAO_ARQUIVO**: Importação via arquivo CSV/Excel
- **API_BANCARIA**: Integração com APIs bancárias
- **TRANSFERENCIA_AUTOMATICA**: Transferências automáticas

### Metas de Economia
**Tipos de Meta:**
- ECONOMIA_MENSAL, ECONOMIA_ANUAL
- RESERVA_EMERGENCIA, INVESTIMENTO_ESPECIFICO
- COMPRA_OBJETO, VIAGEM, EDUCACAO, SAUDE, OUTROS

**Status das Metas:**
- ATIVA, CONCLUIDA, PAUSADA, CANCELADA, VENCIDA

**Funcionalidades:**
- Acompanhamento automático de progresso
- Cálculo de percentual concluído
- Pausar e reativar metas conforme necessário
- Verificação automática de metas vencidas
- Relatórios de performance
- Flexibilidade para reativar metas vencidas

### Assistente Financeiro Inteligente

O sistema inclui um assistente financeiro inteligente que utiliza IA (Ollama Cloud ou Local) para fornecer análises e recomendações personalizadas:

#### Funcionalidades do Assistente
- **Análise de Viabilidade**: Avalia se uma meta financeira é realista baseada na situação atual
- **Geração de Planos de Ação**: Cria estratégias detalhadas para alcançar metas
- **Sugestões de Otimização**: Recomenda melhorias para metas ativas
- **Cálculo de Economia Mensal**: Determina quanto precisa economizar por mês
- **Comparação com Capacidade**: Analisa se a meta é viável com a renda atual

#### Fluxo do Assistente
1. **Definir Meta** → Usuário define uma meta financeira
2. **Sistema Financeiro** → Processa a meta com assistência da IA
3. **Calcular economia mensal** → Calcula valor necessário por mês
4. **Comparar com capacidade atual** → Analisa situação financeira
5. **Gerar Plano de Ação** → Cortes, realocações e investimentos

#### Tecnologias Utilizadas
- **Ollama Cloud/Local**: Serviço de IA (cloud ou servidor local) para execução de modelos de IA
- **Ewerton_Virginio/assitentefinanceiro**: Modelo personalizado baseado em Llama 3.2
- **Spring AI**: Framework para integração com IA
- **Prompts Otimizados**: Templates específicos para análise financeira
- **API Key Authentication**: Autenticação segura via API key para Ollama Cloud

### IA Dinâmica - Resposta Universal

O sistema agora inclui uma IA dinâmica que pode responder a **qualquer pergunta** do usuário, não apenas sobre finanças:

#### Funcionalidades da IA Dinâmica
- **Resposta Universal**: Responde a qualquer prompt, independente do assunto
- **Contexto Financeiro**: Personaliza respostas com dados da conta do usuário
- **Categorização Inteligente**: Identifica automaticamente o tipo de pergunta
- **Memória de Conversação**: Mantém contexto de mensagens anteriores
- **Especialização por Área**: Respostas especializadas por tipo de assunto

#### Tipos de Categorização
- **Metas Financeiras**: Economia, poupança, objetivos financeiros
- **Investimentos**: Aplicações, rendimentos, estratégias de investimento
- **Orçamento**: Controle de gastos, planejamento financeiro
- **Dívidas**: Empréstimos, financiamentos, quitação
- **Renda**: Salários, receitas, aumento de renda
- **Reserva de Emergência**: Fundo de emergência, segurança financeira
- **Planejamento Futuro**: Aposentadoria, planejamento de longo prazo
- **Geral**: Qualquer outro assunto

#### Endpoints Disponíveis
- `/api/ai/dinamica/responder` - Resposta com contexto de conta
- `/api/ai/dinamica/responder-simples` - Resposta simples sem contexto
- `/api/ai/dinamica/conversacao` - Manter conversação com histórico
- `/api/ai/dinamica/teste` - Teste rápido da IA
- `/api/ai/dinamica/responder-dto` - Usando DTO estruturado
- `/api/ai/dinamica/status` - Status da IA dinâmica

#### Exemplos de Uso
```bash
# Pergunta sobre investimentos
curl -X POST /api/ai/dinamica/responder-simples \
  -d '{"prompt": "Como investir em ações?"}'

# Pergunta sobre economia com contexto
curl -X POST /api/ai/dinamica/responder \
  -d '{"prompt": "Como economizar mais?", "contaId": 1}'

# Conversação com histórico
curl -X POST /api/ai/dinamica/conversacao \
  -d '{"prompt": "E sobre fundos?", "historico": ["Como investir?"], "contaId": 1}'
```

### Validações Implementadas
- **Campos Obrigatórios**: Todos os campos essenciais são validados
- **Valores Positivos**: Valores monetários devem ser maiores que zero
- **Datas Válidas**: Validação de datas de início e fim das metas
- **Categorias e Fontes**: Validação de enums obrigatórios
- **IDs Válidos**: Verificação de IDs existentes antes de operações

### Relacionamentos
- **Movimentações** são vinculadas a **Contas** específicas
- **Metas** são vinculadas a **Contas** específicas
- **Usuários** podem ter múltiplas **Contas**
- **Contas** podem ter múltiplas **Movimentações** e **Metas**

## Testes Unitários

### Executar Testes
```bash
# Executar todos os testes
./gradlew test

# Executar testes específicos do módulo financeiro
./gradlew test --tests "*financeiro*"

# Executar testes específicos do módulo de cadastro
./gradlew test --tests "*cadastro*"
```

### Cobertura de Testes

**Total: 53 testes unitários** ✅

#### Módulo Financeiro (53 testes)
- **MovimentacaoFinanceiraServiceTest** (28 testes)
  - Registro de movimentações com cálculo automático de saldo
  - Listagem e busca por ID, conta, período e tipo
  - Atualização, exclusão e estorno de movimentações
  - Cálculo de saldo atual e conversão para DTO
  - Validações de dados e tratamento de erros
  - Validações de categoria, fonte e tipo de movimentação

- **MetaEconomiaServiceTest** (25 testes)
  - Criação e gerenciamento de metas de economia
  - Acompanhamento de progresso e status das metas
  - Busca por conta, tipo e status (ativas, vencidas)
  - Pausar, reativar e verificar metas vencidas
  - Validações de datas, valores e regras de negócio
  - Retorno de número de metas vencidas na verificação

#### Módulo Cadastro (32 testes)
- **CadastroContaServiceTest** (20 testes)
  - Cadastro de contas com validação de duplicatas
  - Listagem, busca por ID e atualização de contas
  - Verificação de estado do banco de dados
  - Validações de campos obrigatórios e IDs

- **CadastroContaControllerTest** (12 testes)
  - Endpoints REST com códigos HTTP corretos
  - Tratamento de erros (400, 404, 500)
  - Validação de entrada e conversão de DTOs

### Padrões de Teste Utilizados

#### Builders Pattern
- **TestDataBuilder** para módulo financeiro
- **CadastroTestDataBuilder** para módulo cadastro
- Criação flexível de objetos de teste
- Valores padrão consistentes e válidos
- Uso de construtores em vez de setters para imutabilidade

#### Técnicas de Teste
- **Mockito** para simulação de dependências
- **JUnit 5** com anotações modernas
- **AAA Pattern** (Arrange, Act, Assert)
- **@DisplayName** em português para clareza

#### Cobertura Completa
- **Casos Positivos**: Todos os cenários de sucesso
- **Casos Negativos**: Todos os cenários de erro
- **Validações**: Todas as regras de validação
- **Edge Cases**: Casos extremos e limites
- **Mensagens de Erro**: Validação de mensagens sem caracteres especiais

### Benefícios dos Testes
- **Confiabilidade**: 100% dos métodos públicos testados
- **Manutenibilidade**: Refatoração segura com detecção de regressões
- **Documentação**: Testes servem como documentação viva
- **Qualidade**: Detecção precoce de bugs e problemas
- **CI/CD**: Integração contínua com validação automática

## Melhorias Implementadas

### Retornos HTTP Aprimorados
- **Respostas Detalhadas**: Endpoints retornam informações completas sobre operações
- **Headers de Erro**: Mensagens de erro específicas em headers HTTP
- **Timestamps**: Informações de data/hora em todas as respostas
- **Contadores**: Número de registros afetados em operações em lote

### Validações Robustas
- **Campos Obrigatórios**: Validação completa de todos os campos essenciais
- **Enums Validados**: Verificação de categorias, fontes e tipos
- **Datas Inteligentes**: Validação flexível de datas para metas existentes
- **Valores Monetários**: Verificação de valores positivos

### Imutabilidade e Segurança
- **Construtores**: Uso de construtores em vez de setters
- **Entidades Imutáveis**: Prevenção de modificações acidentais
- **DTOs Seguros**: Transferência segura de dados

### Logs e Observabilidade
- **Mensagens Limpas**: Logs sem caracteres especiais para compatibilidade
- **Informações Detalhadas**: Logs com contexto completo das operações
- **Rastreabilidade**: Acompanhamento completo do fluxo de dados

---
**Desenvolvido com Spring Boot + IA (OpenAI + Ollama) + Observabilidade + Testes Unitários + Validações Robustas**
