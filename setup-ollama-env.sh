#!/bin/bash

# Script para configurar variáveis de ambiente do Ollama no Linux/macOS
# Execute este script antes de iniciar a aplicação: source setup-ollama-env.sh

echo "========================================"
echo "Configuração do Ollama - Assistente Financeiro"
echo "========================================"
echo ""

# Detectar o shell e arquivo de configuração
if [ -n "$ZSH_VERSION" ]; then
    CONFIG_FILE="$HOME/.zshrc"
    SHELL_NAME="zsh"
elif [ -n "$BASH_VERSION" ]; then
    CONFIG_FILE="$HOME/.bashrc"
    SHELL_NAME="bash"
else
    CONFIG_FILE="$HOME/.profile"
    SHELL_NAME="sh"
fi

# API Key do Ollama
OLLAMA_API_KEY="2533c67150b246b29d0c485a70c91010.c9ZN_GZ4EvrPA1b8DLaljyGo"
OLLAMA_BASE_URL="https://api.ollama.com"
OLLAMA_MODEL="Ewerton_Virginio/assitentefinanceiro"

# Função para adicionar variável ao arquivo de configuração se não existir
add_env_var() {
    local var_name=$1
    local var_value=$2
    
    if grep -q "^export $var_name=" "$CONFIG_FILE" 2>/dev/null; then
        echo "[INFO] $var_name já está configurada em $CONFIG_FILE"
    else
        echo "export $var_name=\"$var_value\"" >> "$CONFIG_FILE"
        echo "[OK] $var_name adicionada ao $CONFIG_FILE"
    fi
}

# Configurar variáveis no arquivo de configuração
echo "[CONFIG] Configurando variáveis de ambiente em $CONFIG_FILE..."
add_env_var "OLLAMA_API_KEY" "$OLLAMA_API_KEY"
add_env_var "OLLAMA_BASE_URL" "$OLLAMA_BASE_URL"
add_env_var "OLLAMA_MODEL" "$OLLAMA_MODEL"

# Exportar para a sessão atual
export OLLAMA_API_KEY="$OLLAMA_API_KEY"
export OLLAMA_BASE_URL="$OLLAMA_BASE_URL"
export OLLAMA_MODEL="$OLLAMA_MODEL"

echo ""
echo "========================================"
echo "Configuração concluída!"
echo "========================================"
echo ""
echo "Variáveis configuradas para esta sessão:"
echo "  OLLAMA_API_KEY=${OLLAMA_API_KEY:0:20}..."
echo "  OLLAMA_BASE_URL=$OLLAMA_BASE_URL"
echo "  OLLAMA_MODEL=$OLLAMA_MODEL"
echo ""
echo "As variáveis foram adicionadas ao $CONFIG_FILE"
echo "Para carregar em novas sessões, execute: source $CONFIG_FILE"
echo ""

