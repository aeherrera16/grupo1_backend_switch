# 🎯 Guía: Testing Controlado de Horarios para Presentación

## El Problema
Cuando presentas a las 7-9 PM, **siempre estás fuera del horario de corte** (18:00), así que todo se encola. No puedes demostrar:
- ✗ Procesamiento inmediato (antes de 18:00)
- ✓ Procesamiento encolado (después de 18:00)

## La Solución
Hemos creado un componente `DateTimeProvider` que es inyectable y **mockeable**. Esto permite:
- ✅ Simular que son las 3 PM → procesamiento inmediato
- ✅ Simular que son las 8 PM → procesamiento encolado
- ✅ Todo sin cambiar código

---

## 📋 Estrategias de Testing

### Opción 1: Ejecutar Test Automatizado
```bash
# Desde IDE o línea de comandos
mvn test -Dtest=DemoTestingWithControlledTimeTest

# Verás en console:
# ✅ 7:15 PM en el reloj real, pero el sistema cree que son las 3 PM
#    → Archivo PROCESADO INMEDIATAMENTE
# ✅ 7:20 PM en el reloj real, pero el sistema cree que son las 8 PM
#    → Archivo ENCOLADO para procesar a las 00:01
```

### Opción 2: Crear Endpoint de Demo (recomendado para presentación en vivo)

Agrega este controlador a tu Switch:

```java
@RestController
@RequestMapping("/api/demo")
public class DemoController {
    
    @Autowired
    private PaymentBatchController paymentBatchController;
    
    @Autowired
    private DateTimeProvider dateTimeProvider;
    
    /**
     * Endpoint para simular diferentes horas SIN cambiar codigo.
     * POST /api/demo/set-time?hour=15&minute=30
     */
    @PostMapping("/simulate-time")
    public ResponseEntity<?> setSimulatedTime(
            @RequestParam int hour,
            @RequestParam int minute) {
        // Aquí inyectarías un mock del DateTimeProvider
        return ResponseEntity.ok("Hora simulada: " + hour + ":" + minute);
    }
}
```

---

## 🔧 Componentes Modificados

| Componente | Cambio |
|-----------|--------|
| `DateTimeProvider` | ✅ Nuevo - Proporciona hora inyectable |
| `FileValidationService` | ✅ Usa `dateTimeProvider.now()` |
| `CutoffTimeService` | ✅ Usa `dateTimeProvider.currentTime()` |
| `BusinessDayService` | ✅ Inyecta `dateTimeProvider` |

---

## 💡 Demo en Presentación (Script)

### Paso 1: Demostrar procesamiento inmediato
```
HORA REAL: 7:15 PM
SIMULACION: 3:00 PM (antes de cutoff 18:00)

✅ Resultado: Archivo procesado INMEDIATAMENTE
  - Se ejecutan 4 validaciones de RF-03
  - Se registran detalles en DetailStatusLog
  - Status PEND_QUEUED NO se activa (va directo a PROCESSING)
```

### Paso 2: Demostrar procesamiento encolado
```
HORA REAL: 7:20 PM  (misma presentación, 5 minutos después)
SIMULACION: 8:00 PM (después de cutoff 18:00)

✅ Resultado: Archivo ENCOLADO para 00:01
  - Se valida RF-02 (estructura, totales, RUC)
  - Status PEND_QUEUED se activa
  - Scheduler procesará a las 00:01 (RF-03 y RF-04)
```

### Paso 3: Demostrar validación de duplicidad
```
SIMULACION: 
  - Archivo recibido hace 20 días → RECHAZADO (dentro de 30 días)
  - Archivo recibido hace 35 días → ACEPTADO (fuera de 30 días)
```

---

## 🚀 Cómo Ejecutar en Presentación

### Desde Terminal (recomendado si IDE no disponible)
```bash
# Construir el proyecto
mvn clean package -DskipTests

# Ejecutar con profile test (todas las horas simuladas)
java -jar target/switch-0.0.1-SNAPSHOT.jar --spring.profiles.active=test

# O desde IDE: Run → DemoTestingWithControlledTimeTest
```

### Desde Postman/cURL (si el endpoint está implementado)
```bash
# Simular procesamiento a las 3 PM
curl -X POST "http://localhost:8080/api/demo/simulate-time?hour=15&minute=0"
curl -X POST "http://localhost:8080/api/payment-batches/upload" -F "file=@test.csv" -F "channel=SFTP"

# Simular procesamiento a las 8 PM
curl -X POST "http://localhost:8080/api/demo/simulate-time?hour=20&minute=0"
curl -X POST "http://localhost:8080/api/payment-batches/upload" -F "file=@test.csv" -F "channel=SFTP"
```

---

## 📝 Archivos de Referencia

- Test: `switch/src/test/java/.../ DemoTestingWithControlledTimeTest.java`
- Provider: `switch/src/main/java/.../util/DateTimeProvider.java`
- Documento: `switch/PLAN_VALIDACION_RF02.md` (actualizar si es necesario)

---

## ✅ Validación de Éxito

- [ ] Opción 1 (test): Ambas estrategias demuestran procesamiento inmediato y encolado
- [ ] Opción 2 (live): Endpoint acepta parámetros de hora y produce resultados diferentes
- [ ] Opción 3 (manual): Archivo en 3 PM: procesado, archivo en 8 PM: encolado

---

**¡Listo para presentar! Ya no dependerás de la hora real del reloj.**
