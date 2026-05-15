# Configuración del Servicio SFTP - switch-email-service

## Requisitos Previos

1. **Servidor SFTP corriendo** en el puerto 22 (o el puerto que configures)
2. **Java 21** instalado
3. **Maven** instalado

## Opción 1: Usar Docker para Servidor SFTP (Recomendado)

Esta es la opción más simple si tienes Docker instalado:

```powershell
# Ejecutar servidor SFTP con Docker
docker run -d -p 22:22 atmoz/sftp:alpine sftpuser:password:::upload

# Verificar que está corriendo
docker ps
```

Esto creará un servidor SFTP con:
- Usuario: `sftpuser`
- Password: `password`
- Directorio: `/upload`
- Puerto: `22`

## Opción 2: Usar Servidor SFTP Externo

Si tienes acceso a un servidor SFTP externo, configura las variables de entorno con los datos de ese servidor.

## Configuración de Variables de Entorno

### Método 1: Script PowerShell (Temporal)

```powershell
# Ejecutar el script
.\setup-sftp-env.ps1

# Si tienes problemas con la política de ejecución
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
.\setup-sftp-env.ps1
```

### Método 2: Variables de Entorno Permanentes

```powershell
setx SERVER_PORT "8082"
setx SFTP_HOST "localhost"
setx SFTP_PORT "22"
setx SFTP_USERNAME "sftpuser"
setx SFTP_PASSWORD "password"
setx SFTP_REMOTE_DIRECTORY "/upload"
setx SFTP_LOCAL_DIRECTORY "./sftp-downloads"
setx SFTP_INTEGRATION_ENABLED "true"
setx SFTP_SCHEDULER_ENABLED "true"
setx SFTP_SCHEDULER_INTERVAL "60000"
```

**Nota:** Después de usar `setx`, cierra y abre una nueva terminal para que los cambios surtan efecto.

### Método 3: Archivo .env

Crea un archivo `.env` en el directorio del servicio:

```bash
SERVER_PORT=8082
SFTP_HOST=localhost
SFTP_PORT=22
SFTP_USERNAME=sftpuser
SFTP_PASSWORD=password
SFTP_REMOTE_DIRECTORY=/upload
SFTP_LOCAL_DIRECTORY=./sftp-downloads
SFTP_INTEGRATION_ENABLED=true
SFTP_SCHEDULER_ENABLED=true
SFTP_SCHEDULER_INTERVAL=60000
SWITCH_API_BASE_URL=http://localhost:8081
```

Luego carga las variables:
```powershell
# En PowerShell
Get-Content .env | ForEach-Object { $var = $_.Split('='); [Environment]::SetEnvironmentVariable($var[0], $var[1]) }
```

## Ejecutar el Servicio

```powershell
# Compilar el proyecto
mvn clean package

# Ejecutar el servicio
mvn spring-boot:run

# O ejecutar el JAR compilado
java -jar target/switch-email-service-1.0.0.jar
```

## Probar la Conexión SFTP

### Con FileZilla

1. Abre FileZilla
2. Configura la conexión:
   - Host: `localhost`
   - Puerto: `22`
   - Protocolo: `SFTP`
   - Usuario: `sftpuser`
   - Password: `password`
3. Conecta y sube archivos CSV al directorio `/upload`

### Con PowerShell (prueba de conexión)

```powershell
# Prueba de conexión SFTP con PowerShell
$password = ConvertTo-SecureString "password" -AsPlainText -Force
$credential = New-Object System.Management.Automation.PSCredential ("sftpuser", $password)
New-SFTPSession -ComputerName localhost -Credential $credential -Port 22
```

## Verificar que el Servicio Funciona

1. **Verificar logs del servicio:**
   ```
   Conectado exitosamente al servidor SFTP: localhost:22
   Encontrados X archivos CSV en /upload
   Archivo X descargado y eliminado del servidor
   Archivo enviado exitosamente al switch
   ```

2. **Verificar endpoint de health:**
   ```
   http://localhost:8082/actuator/health
   ```

3. **Verificar directorio local:**
   ```
   ./sftp-downloads/processed/  # Archivos procesados exitosamente
   ./sftp-downloads/errors/     # Archivos con errores
   ```

## Solución de Problemas

### Error: "No se pudo conectar al servidor SFTP"

- Verifica que el servidor SFTP esté corriendo: `docker ps`
- Verifica que el puerto 22 no esté en uso por otro servicio
- Verifica las credenciales (usuario y password)

### Error: "Integración SFTP deshabilitada"

- Verifica que `SFTP_INTEGRATION_ENABLED=true`
- Verifica que `SFTP_SCHEDULER_ENABLED=true`

### Error: "ECONNREFUSED - Conexión rechazada"

- El servidor SFTP no está corriendo en el puerto 22
- Inicia el servidor SFTP con Docker o instala OpenSSH Server

### Error de permisos en Windows

- Ejecuta PowerShell como Administrador para instalar OpenSSH Server
- O usa Docker que no requiere permisos elevados

## Flujo de Trabajo del Servicio

1. El servicio se conecta al servidor SFTP cada 60 segundos (configurable)
2. Descarga archivos CSV del directorio `/upload`
3. Elimina los archivos del servidor SFTP después de descargarlos
4. Envía los archivos al Switch principal (http://localhost:8081)
5. Mueve los archivos a `./sftp-downloads/processed/` si tienen éxito
6. Mueve los archivos a `./sftp-downloads/errors/` si fallan

## Configuración Adicional

Para cambiar el intervalo de procesamiento:

```powershell
setx SFTP_SCHEDULER_INTERVAL "30000"  # 30 segundos
```

Para deshabilitar el scheduler y procesar manualmente:

```powershell
setx SFTP_SCHEDULER_ENABLED "false"
```

Llama al endpoint manualmente:
```
POST http://localhost:8082/api/sftp/process
```
