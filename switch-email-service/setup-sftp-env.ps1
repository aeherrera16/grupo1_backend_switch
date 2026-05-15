# Script para configurar variables de entorno del servicio SFTP
# Ejecutar: .\setup-sftp-env.ps1

Write-Host "Configurando variables de entorno para switch-email-service..." -ForegroundColor Green

# Configuración del servidor
$env:SERVER_PORT="8082"

# Configuración del Switch API
$env:SWITCH_API_BASE_URL="http://localhost:8081"
$env:SWITCH_API_ENDPOINT="/api/payment-batch/upload-from-sftp-buzon"
$env:SWITCH_API_TIMEOUT="30000"

# Configuración SFTP Cliente (para conectarse a servidor externo)
$env:SFTP_HOST="localhost"
$env:SFTP_PORT="22"
$env:SFTP_USERNAME="sftpuser"
$env:SFTP_PASSWORD="123"
$env:SFTP_REMOTE_DIRECTORY="/upload"
$env:SFTP_LOCAL_DIRECTORY="./sftp-downloads"
$env:SFTP_INTEGRATION_ENABLED="false"
$env:SFTP_SCHEDULER_ENABLED="false"
$env:SFTP_SCHEDULER_INTERVAL="60000"

# Configuración Servidor SFTP Embebido (para que este servicio actúe como servidor)
$env:SFTP_SERVER_ENABLED="true"
$env:SFTP_SERVER_PORT="22"
$env:SFTP_SERVER_HOST="0.0.0.0"
$env:SFTP_SERVER_UPLOAD_DIRECTORY="./sftp-uploads"
$env:SFTP_SERVER_USERNAME="sftpuser"
$env:SFTP_SERVER_PASSWORD="password"

# Logging
$env:LOGGING_LEVEL_ROOT="INFO"
$env:LOGGING_LEVEL_EC_EDU_ESPE_BANQUITO_EMAILSERVICE="DEBUG"

Write-Host "Variables de entorno configuradas:" -ForegroundColor Yellow
Write-Host "SERVER_PORT=$env:SERVER_PORT"
Write-Host "SFTP_SERVER_ENABLED=$env:SFTP_SERVER_ENABLED"
Write-Host "SFTP_SERVER_PORT=$env:SFTP_SERVER_PORT"
Write-Host "SFTP_SERVER_USERNAME=$env:SFTP_SERVER_USERNAME"
Write-Host "SFTP_SERVER_UPLOAD_DIRECTORY=$env:SFTP_SERVER_UPLOAD_DIRECTORY"
Write-Host ""
Write-Host "Para hacer permanentes estas variables, ejecuta:" -ForegroundColor Cyan
Write-Host "setx SERVER_PORT `"8082`""
Write-Host "setx SFTP_SERVER_ENABLED `"true`""
Write-Host "setx SFTP_SERVER_PORT `"22`""
Write-Host "setx SFTP_SERVER_USERNAME `"sftpuser`""
Write-Host "setx SFTP_SERVER_PASSWORD `"password`""
Write-Host "setx SFTP_SERVER_UPLOAD_DIRECTORY `"./sftp-uploads`""
