# Guía para Servidor SFTP Embebido - switch-email-service

## Cambios Realizados

Tu programa ahora puede funcionar como **SERVIDOR SFTP** además de cliente SFTP.

### Archivos Creados/Modificados:

1. **pom.xml** - Agregadas dependencias Apache SSHD para servidor SFTP
2. **SftpServerConfig.java** - Configuración del servidor SFTP embebido
3. **SftpServerService.java** - Servicio que gestiona el servidor SFTP
4. **application.properties** - Agregada configuración del servidor SFTP
5. **setup-sftp-env.ps1** - Actualizado con variables del servidor SFTP

## Instalación de Maven (Requisito)

### Opción 1: Instalar Maven con Chocolatey (Recomendado)

```powershell
# Instalar Chocolatey si no está instalado
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Instalar Maven
choco install maven -y

# Verificar instalación
mvn --version
```

### Opción 2: Instalar Maven Manualmente

1. Descargar Maven desde: https://maven.apache.org/download.cgi
2. Extraer en `C:\Program Files\Maven`
3. Agregar al PATH:
   - Variables de entorno -> PATH
   - Agregar: `C:\Program Files\Maven\bin`
4. Reiniciar PowerShell
5. Verificar: `mvn --version`

## Configuración y Ejecución

### Paso 1: Configurar Variables de Entorno

```powershell
# Ejecutar el script de configuración
.\setup-sftp-env.ps1

# Si tienes problemas con la política de ejecución
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
.\setup-sftp-env.ps1
```

### Paso 2: Compilar el Proyecto

```powershell
mvn clean compile
```

### Paso 3: Ejecutar el Servicio

```powershell
mvn spring-boot:run
```

### Paso 4: Verificar que el Servidor SFTP está Corriendo

Deberías ver en los logs:
```
Servidor SFTP iniciado exitosamente en el puerto 22
Directorio de uploads SFTP: C:\...\sftp-uploads
```

## Probar la Conexión SFTP con FileZilla

### Configuración en FileZilla:

1. **Host:** `localhost`
2. **Puerto:** `22`
3. **Protocolo:** `SFTP - SSH File Transfer Protocol`
4. **Tipo de inicio de sesión:** `Normal`
5. **Usuario:** `sftpuser`
6. **Contraseña:** `password`

### Subir Archivos de Prueba:

1. Conecta con FileZilla
2. Navega al directorio raíz
3. Sube un archivo CSV de prueba
4. El archivo aparecerá en `./sftp-uploads/`

## Probar con PowerShell (Alternativa)

```powershell
# Instalar módulo SFTP si no está instalado
Install-Module -Name Posh-SSH -Force

# Conectar al servidor SFTP
$password = ConvertTo-SecureString "password" -AsPlainText -Force
$credential = New-Object System.Management.Automation.PSCredential ("sftpuser", $password)
$session = New-SFTPSession -ComputerName localhost -Credential $credential -Port 22

# Listar archivos
Get-SFTPChildItem -SessionId $session.SessionId

# Subir archivo
Set-SFTPFile -SessionId $session.SessionId -LocalFile "test.csv" -RemotePath "/test.csv"

# Cerrar sesión
Remove-SFTPSession -SessionId $session.SessionId
```

## Flujo de Trabajo del Servidor SFTP

1. **Cliente conecta** al puerto 22 con usuario `sftpuser`
2. **Sube archivos CSV** al directorio `/upload`
3. **Servidor procesa** automáticamente los archivos
4. **Envía al Switch** principal (http://localhost:8081)
5. **Mueve archivos** a:
   - `./sftp-uploads/processed/` - Éxito
   - `./sftp-uploads/errors/` - Error

## Variables de Entorno Importantes

| Variable | Valor por Defecto | Descripción |
|----------|------------------|-------------|
| `SFTP_SERVER_ENABLED` | `false` | Habilita servidor SFTP |
| `SFTP_SERVER_PORT` | `22` | Puerto del servidor SFTP |
| `SFTP_SERVER_HOST` | `0.0.0.0` | Host del servidor SFTP |
| `SFTP_SERVER_USERNAME` | `sftpuser` | Usuario SFTP |
| `SFTP_SERVER_PASSWORD` | `password` | Password SFTP |
| `SFTP_SERVER_UPLOAD_DIRECTORY` | `./sftp-uploads` | Directorio de uploads |

## Solución de Problemas

### Error: "Puerto 22 ya está en uso"

Cambia el puerto del servidor SFTP:
```powershell
$env:SFTP_SERVER_PORT="2222"
```

### Error: "No se pudo iniciar el servidor SFTP"

- Verifica que no haya otro servicio usando el puerto 22
- Ejecuta PowerShell como Administrador si usas puerto 22
- Usa un puerto diferente (ej: 2222)

### Error: "Falló autenticación"

Verifica las credenciales:
```powershell
$env:SFTP_SERVER_USERNAME="sftpuser"
$env:SFTP_SERVER_PASSWORD="password"
```

### Error: "Maven no encontrado"

Instala Maven siguiendo las instrucciones de arriba.

## Arquitectura Actual

```
Cliente SFTP (FileZilla, etc.)
    ↓ (conecta a puerto 22)
switch-email-service (SERVIDOR SFTP)
    ↓ (procesa archivos CSV)
Switch Principal (puerto 8081)
```

## Modos de Operación

### Modo 1: Servidor SFTP (Actual)
- `SFTP_SERVER_ENABLED=true`
- `SFTP_INTEGRATION_ENABLED=false`
- Tu programa ACTÚA como servidor SFTP

### Modo 2: Cliente SFTP (Original)
- `SFTP_SERVER_ENABLED=false`
- `SFTP_INTEGRATION_ENABLED=true`
- Tu programa se CONECTA a servidor SFTP externo

## Verificación de Salud

```powershell
# Verificar que el servidor SFTP está corriendo
curl http://localhost:8082/actuator/health

# Verificar información del servidor
curl http://localhost:8082/actuator/info
```

## Notas de Seguridad

⚠️ **ADVERTENCIA:** Esta configuración es para desarrollo. Para producción:

1. Usa autenticación por clave pública en lugar de password
2. Usa puertos no estándar (no 22)
3. Configura firewall apropiadamente
4. Usa HTTPS/TLS para el endpoint HTTP
5. Implementa rate limiting
6. Usa passwords fuertes o variables de entorno seguras

## Próximos Pasos

1. Instalar Maven
2. Ejecutar `.\setup-sftp-env.ps1`
3. Compilar con `mvn clean compile`
4. Ejecutar con `mvn spring-boot:run`
5. Probar conexión con FileZilla
6. Subir archivo CSV de prueba
7. Verificar que se procesa correctamente
