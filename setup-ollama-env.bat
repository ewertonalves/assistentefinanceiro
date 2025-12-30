@echo off
REM Script para configurar variáveis de ambiente do Ollama no Windows
REM Execute este script antes de iniciar a aplicação

echo ========================================
echo Configuracao do Ollama - Assistente Financeiro
echo ========================================
echo.

REM Verificar se a API key já está configurada
if defined OLLAMA_API_KEY (
    echo [INFO] OLLAMA_API_KEY ja esta configurada
    echo [INFO] Valor atual: %OLLAMA_API_KEY:~0,20%...
) else (
    echo [CONFIG] Configurando OLLAMA_API_KEY...
    setx OLLAMA_API_KEY "2533c67150b246b29d0c485a70c91010.c9ZN_GZ4EvrPA1b8DLaljyGo" /M
    set OLLAMA_API_KEY=2533c67150b246b29d0c485a70c91010.c9ZN_GZ4EvrPA1b8DLaljyGo
    echo [OK] OLLAMA_API_KEY configurada com sucesso
)

REM Configurar base URL se não estiver definida
if not defined OLLAMA_BASE_URL (
    echo [CONFIG] Configurando OLLAMA_BASE_URL...
    setx OLLAMA_BASE_URL "https://api.ollama.com" /M
    set OLLAMA_BASE_URL=https://api.ollama.com
    echo [OK] OLLAMA_BASE_URL configurada: https://api.ollama.com
) else (
    echo [INFO] OLLAMA_BASE_URL ja esta configurada: %OLLAMA_BASE_URL%
)

REM Configurar modelo se não estiver definido
if not defined OLLAMA_MODEL (
    echo [CONFIG] Configurando OLLAMA_MODEL...
    setx OLLAMA_MODEL "Ewerton_Virginio/assitentefinanceiro" /M
    set OLLAMA_MODEL=Ewerton_Virginio/assitentefinanceiro
    echo [OK] OLLAMA_MODEL configurado: Ewerton_Virginio/assitentefinanceiro
) else (
    echo [INFO] OLLAMA_MODEL ja esta configurado: %OLLAMA_MODEL%
)

echo.
echo ========================================
echo Configuracao concluida!
echo ========================================
echo.
echo IMPORTANTE: As variaveis foram configuradas permanentemente no sistema.
echo Para usar nesta sessao, execute:
echo   set OLLAMA_API_KEY=2533c67150b246b29d0c485a70c91010.c9ZN_GZ4EvrPA1b8DLaljyGo
echo   set OLLAMA_BASE_URL=https://api.ollama.com
echo   set OLLAMA_MODEL=Ewerton_Virginio/assitentefinanceiro
echo.
echo Ou reinicie o terminal para carregar as variaveis automaticamente.
echo.

