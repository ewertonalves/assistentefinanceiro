# Configuração Segura do Ollama

Este guia explica como configurar a API key do Ollama de forma segura e robusta.

## 🚀 Configuração Rápida

### Windows

Execute o script de configuração:

```cmd
setup-ollama-env.bat
```

O script irá:
- Configurar a API key do Ollama
- Configurar a base URL
- Configurar o modelo
- Tornar as configurações permanentes no sistema

**Nota:** Após executar o script, você pode precisar reiniciar o terminal ou executar manualmente para a sessão atual:

```cmd
set OLLAMA_API_KEY=2533c67150b246b29d0c485a70c91010.c9ZN_GZ4EvrPA1b8DLaljyGo
set OLLAMA_BASE_URL=https://api.ollama.com
set OLLAMA_MODEL=Ewerton_Virginio/assitentefinanceiro
```

### Linux/macOS

Execute o script de configuração:

```bash
source setup-ollama-env.sh
```

O script irá:
- Adicionar as variáveis ao seu arquivo de configuração do shell (~/.bashrc ou ~/.zshrc)
- Exportar as variáveis para a sessão atual
- Configurar tudo automaticamente

**Nota:** Para carregar em novas sessões, execute:
```bash
source ~/.bashrc  # ou source ~/.zshrc
```

## Segurança

### O que está seguro:

- API key **NÃO** está hardcoded no código
- API key **NÃO** está no repositório Git (`.gitignore` configurado)
- Validação automática na inicialização da aplicação
- Logs mascarados (não exibem a API key completa)

### Boas Práticas:

1. **Nunca commite** arquivos `.env` ou scripts com API keys
2. **Use variáveis de ambiente** em produção
3. **Rotacione a API key** periodicamente
4. **Use gerenciadores de secrets** (AWS Secrets Manager, HashiCorp Vault) em ambientes de produção

## Validação

Após configurar, execute a aplicação. Você verá nos logs:

### Configuração Válida:
```
========================================
Validando configuração do Ollama...
========================================
Modo: Ollama Cloud
Base URL: https://api.ollama.com
API Key: 2533c671...aljyGo (configurada)
Modelo: Ewerton_Virginio/assitentefinanceiro
Status: CONFIGURADO COM SUCESSO
========================================
Configuração do Ollama: VALIDADA
========================================
```

### Configuração com Problemas:
```
========================================
ERRO: OLLAMA_API_KEY não configurada!
========================================
A aplicação está configurada para usar Ollama Cloud, mas a API key não foi fornecida.

SOLUÇÕES:
1. Execute o script de configuração:
   Windows: setup-ollama-env.bat
   Linux/macOS: source setup-ollama-env.sh
...
```

## 🛠️ Configuração Manual

Se preferir configurar manualmente:

### Windows (PowerShell)
```powershell
$env:OLLAMA_API_KEY="2533c67150b246b29d0c485a70c91010.c9ZN_GZ4EvrPA1b8DLaljyGo"
$env:OLLAMA_BASE_URL="https://api.ollama.com"
$env:OLLAMA_MODEL="Ewerton_Virginio/assitentefinanceiro"
```

### Windows (CMD)
```cmd
set OLLAMA_API_KEY=2533c67150b246b29d0c485a70c91010.c9ZN_GZ4EvrPA1b8DLaljyGo
set OLLAMA_BASE_URL=https://api.ollama.com
set OLLAMA_MODEL=Ewerton_Virginio/assitentefinanceiro
```

### Linux/macOS
```bash
export OLLAMA_API_KEY="2533c67150b246b29d0c485a70c91010.c9ZN_GZ4EvrPA1b8DLaljyGo"
export OLLAMA_BASE_URL="https://api.ollama.com"
export OLLAMA_MODEL="Ewerton_Virginio/assitentefinanceiro"
```

## Variáveis de Ambiente

| Variável | Descrição | Obrigatória (Cloud) | Obrigatória (Local) |
|----------|-----------|---------------------|---------------------|
| `OLLAMA_API_KEY` | API key do Ollama Cloud | Sim | ❌ Não |
| `OLLAMA_BASE_URL` | URL base do Ollama | Sim | Sim |
| `OLLAMA_MODEL` | Nome do modelo a usar | ❌ Não (tem padrão) | ❌ Não (tem padrão) |

## Alternando entre Cloud e Local

### Para usar Ollama Cloud:
```properties
# application.properties
ollama.base-url=${OLLAMA_BASE_URL:https://api.ollama.com}
ollama.api-key=${OLLAMA_API_KEY:}
```

### Para usar Ollama Local:
```properties
# application.properties
ollama.base-url=${OLLAMA_BASE_URL:http://localhost:11434}
ollama.api-key=${OLLAMA_API_KEY:}  # Pode deixar vazio
```

## Troubleshooting

### Problema: "OLLAMA_API_KEY não configurada"

**Solução:**
1. Execute o script de configuração apropriado
2. Verifique se a variável está definida: `echo $OLLAMA_API_KEY` (Linux/macOS) ou `echo %OLLAMA_API_KEY%` (Windows)
3. Reinicie o terminal após configurar

### Problema: "API key parece estar incompleta"

**Solução:**
- Verifique se copiou a API key completa
- A API key deve ter pelo menos 20 caracteres

### Problema: Script não funciona no Windows

**Solução:**
- Execute como Administrador
- Ou configure manualmente usando `setx` ou variáveis de ambiente do sistema

## Documentação Adicional

Para mais informações, consulte:
- `HELP.md` - Documentação completa do sistema
- Seção "Configuração de Variáveis de Ambiente" no HELP.md

