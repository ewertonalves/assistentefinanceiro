@echo off
echo Configurando modelo personalizado do Ollama...
echo.

echo 1. Baixando modelo base llama3.2...
ollama pull llama3.2

echo.
echo 2. Criando modelo personalizado Ewerton_Virginio/assitentefinanceiro...
ollama create -f Modelfile Ewerton_Virginio/assitentefinanceiro

echo.
echo 3. Verificando modelos instalados...
ollama list

echo.
echo 4. Testando o modelo...
ollama run Ewerton_Virginio/assitentefinanceiro "Olá, como você pode me ajudar com finanças?"

echo.
echo Configuração concluída! O modelo Ewerton_Virginio/assitentefinanceiro está pronto para uso.
pause
