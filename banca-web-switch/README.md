# Banca Web Switch

Frontend Node.js para consumir el Core bancario y el Switch de pagos masivos desde una sola interfaz.

## Requisitos

- Node.js 18+
- Core levantado en `http://localhost:8080`
- Switch levantado en `http://localhost:8081`

## Ejecutar

```bash
cd banca-web-switch
npm start
```

Abre `http://localhost:5173`.

## Variables opcionales

Puedes definirlas en el sistema o crear un archivo `.env` dentro de `banca-web-switch` usando `.env.example` como base.

```bash
PORT=5173
CORE_BASE_URL=http://localhost:8080
SWITCH_BASE_URL=http://localhost:8081
```

## Flujos incluidos

- Login para cliente natural o jurídico validado en Core con `/core/v1/auth/customers/login`.
- Consulta de cuentas del cliente en Core.
- Panel jurídico para carga CSV al Switch con canal `WEB`.
- Consulta de lotes, procesamiento manual, resumen, detalle, historial, comprobante y novedades.

El servidor Node expone un proxy local:

- `/api/core/*` hacia el Core.
- `/api/switch/*` hacia el Switch.
