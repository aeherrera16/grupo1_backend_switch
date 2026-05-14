# 🎯 Plan de Validación RF-02 - Portal Web + SFTP

## 📋 Escenario de Prueba Completo

### 🔄 **Flujo 1: Portal Web (Manual)**
1. **Subir archivo vía Portal Web**
2. **Validación RF-02 temprana**
3. **Resultado esperado**: Procesamiento exitoso o rechazo

### 🔄 **Flujo 2: SFTP (Automático)**
1. **Colocar archivo en sftp-files/pagos/**
2. **LocalFileProcessor procesa automáticamente**
3. **Resultado esperado**: Movimiento a procesados/ o errores/

### 🔄 **Flujo 3: Detección de Duplicados Cruzados**
1. **Subir mismo archivo por Portal Web**
2. **Intentar subir mismo archivo por SFTP**
3. **Resultado esperado**: Segundo intento rechazado

---

## 🧪 **Pasos para Validar**

### **Paso 1: Validar Portal Web**
```bash
# 1. Iniciar aplicación Spring Boot
# 2. Ir a http://localhost:8080
# 3. Subir archivo: test_sftp.csv
# 4. Observar logs de validación RF-02
```

### **Paso 2: Validar SFTP**
```bash
# 1. Asegurar que test_sftp.csv esté en sftp-files/pagos/
# 2. Esperar 30 segundos (ejecución automática)
# 3. Observar logs de LocalFileProcessor
# 4. Verificar archivo en procesados/ o errores/
```

### **Paso 3: Validar Detección de Duplicados**
```bash
# 1. Subir archivo por Portal Web (debe ser exitoso)
# 2. Copiar mismo archivo a sftp-files/pagos/
# 3. SFTP debe rechazar por duplicado (mismo hash)
# 4. Ver logs de detección de duplicados
```

---

## 📊 **Resultados Esperados**

### ✅ **Portal Web Exitoso**
- Logs: `=== NUEVA SOLICITUD DE UPLOAD CSV ===`
- Validación: `✅ Validación temprana exitosa`
- Resultado: `SUCCESS` y batch `VALIDATED`

### ✅ **SFTP Exitoso**
- Logs: `=== INICIANDO PROCESAMIENTO LOCAL ===`
- Validación: `✅ Validación temprana local exitosa`
- Resultado: Archivo movido a `procesados/`

### ❌ **Duplicado Detectado**
- Logs: `❌ DUPLICADO DETECTADO: Hash xxx ya fue procesado`
- Acción: Archivo movido a `errores/`
- Mensaje: "Archivo duplicado detectado"

---

## 🔍 **Logs Clave a Observar**

### **Portal Web:**
```
=== NUEVA SOLICITUD DE UPLOAD CSV ===
🔍 Iniciando validación temprana RF-02...
=== INICIO VALIDACIÓN TEMPRANA RF-02 ===
✅ Validación temprana exitosa
```

### **SFTP:**
```
🔄 Iniciando procesamiento local de archivos...
=== INICIANDO PROCESAMIENTO LOCAL: test_sftp.csv ===
🔍 Iniciando validación temprana RF-02 LOCAL...
✅ Validación temprana local exitosa
```

### **Duplicados:**
```
❌ DUPLICADO DETECTADO: Hash abc123 ya fue procesado con éxito
❌ RECHAZO TEMPRANO RF-02: Archivo duplicado detectado
```

---

## 🎯 **Criterios de Éxito**

- ✅ **Portal Web**: Valida y procesa archivos correctamente
- ✅ **SFTP**: Procesa automáticamente cada 30 segundos
- ✅ **Duplicados**: Detecta y rechaza archivos duplicados
- ✅ **Consistencia**: Ambos canales usan misma validación RF-02
- ✅ **Logs**: Información detallada para debugging

---

## 🚀 **Ejecutar Validación**

1. **Reiniciar aplicación Spring Boot**
2. **Seguir pasos en orden indicado**
3. **Documentar resultados en sección below**
4. **Verificar cumplimiento RF-02 completo**
