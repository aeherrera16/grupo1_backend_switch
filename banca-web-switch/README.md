# Banca Web Switch

Frontend de banca web para consumir el Core bancario y el Switch de pagos masivos.

## Stack

- Vite
- TypeScript
- Tailwind CSS
- Node.js para el proxy local y la entrega de la build compilada

## Requisitos

- Node.js 18+
- Core levantado en `http://localhost:8080`
- Switch levantado en `http://localhost:8081`

## Ejecutar en desarrollo

```bash
cd banca-web-switch
npm install
npm run dev
```

Vite levanta la UI en `http://localhost:5173`.

## Servidor Node / proxy

```bash
npm start
```

El servidor Node queda en `http://localhost:4173` y sigue exponiendo:

- `/api/core/*` hacia el Core.
- `/api/switch/*` hacia el Switch.

## Build de produccion

```bash
npm run build
npm start
```

La build se genera en `dist/` y el servidor Node la sirve si existe.

## Variables opcionales

Puedes definirlas en el sistema o crear un archivo `.env` dentro de `banca-web-switch` usando `.env.example` como base.

```bash
PORT=4173
CORE_BASE_URL=http://localhost:8080
SWITCH_BASE_URL=http://localhost:8081
```

## Flujos incluidos

- Login para cliente natural o jurídico validado en Core con `/core/v1/auth/customers/login`.
- Consulta de cuentas del cliente en Core.
- Panel jurídico para carga CSV al Switch con canal `WEB`.
- Consulta de lotes, procesamiento manual, resumen, detalle, historial, comprobante y novedades.
