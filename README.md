# Banco BanQuito - Monolito Unificado

Repositorio unificado para el **Core de Cuentas** y **Switch de Pagos Masivos** del Banco BanQuito.

## 📁 Estructura de Proyecto

```
grupo1_BancoBanQuito/
├── backend/
│   ├── banquito-core/                    ← Core Financiero
│   │   ├── src/main/java/.../core/
│   │   ├── src/main/resources/
│   │   ├── pom.xml
│   │   └── README.md (pendiente)
│   │
│   └── switch-pagos/                     ← Switch de Pagos Masivos
│       ├── src/main/java/.../switchpagos/
│       ├── src/main/resources/
│       ├── pom.xml
│       └── README.md (pendiente)
│
├── docs/
│   ├── DIVISIÓN_TRABAJO.md               ← Responsabilidades por persona
│   ├── SETUP.md                          ← Cómo levantar localmente
│   ├── ARQUITECTURA.md                   ← Visión general del proyecto
│   ├── REQUISITOS_RF.md                  ← Mapeo de requisitos funcionales
│   └── INTEGRACIÓN_CORE_SWITCH.md        ← Cómo se comunican
│
├── .github/
│   └── workflows/                        ← CI/CD (próximas)
│
├── infra/
│   ├── docker/
│   │   ├── docker-compose.yml            ← Dev local
│   │   └── Dockerfile
│   └── scripts/
│       ├── init-db.sh
│       └── build.sh
│
├── .gitignore
├── README.md
└── pom.xml                               ← POM Padre (opcional, para coordinar builds)
```

## 🚀 Inicio Rápido

### Requisitos
- **Java 21+**
- **Maven 3.8+**
- **Git**
- **Docker** (opcional, para BD)

### Clonar y Configurar

```bash
# Clonar el repo unificado
git clone https://github.com/aeherrera16/grupo1_BancoBanQuito.git
cd grupo1_BancoBanQuito

# Ver las ramas disponibles
git branch -a
```

### Levantar Localmente

Opción 1: **Con Docker Compose** (recomendado)
```bash
cd infra/docker
docker-compose up -d
```

Opción 2: **Manualmente**
```bash
# Core (puerto 8080)
cd backend/banquito-core
mvn clean install
mvn spring-boot:run

# Switch (en otra terminal, puerto 8081)
cd backend/switch-pagos
mvn clean install
mvn spring-boot:run
```

### Tests

```bash
# Core
cd backend/banquito-core
mvn test

# Switch
cd backend/switch-pagos
mvn test
```

## 👥 División del Trabajo

Consulta [docs/DIVISIÓN_TRABAJO.md](docs/DIVISIÓN_TRABAJO.md) para ver asignación de personas por módulo/funcionalidad.

## 📚 Documentación

- **[SETUP.md](docs/SETUP.md)** — Instrucciones detalladas de configuración
- **[ARQUITECTURA.md](docs/ARQUITECTURA.md)** — Visión general del sistema
- **[REQUISITOS_RF.md](docs/REQUISITOS_RF.md)** — Requisitos funcionales (RF-01 a RF-08)
- **[INTEGRACIÓN_CORE_SWITCH.md](docs/INTEGRACIÓN_CORE_SWITCH.md)** — Cómo Core y Switch se comunican

## 🔄 Git Subtree - Sincronización

Este repo está conformado por dos submódulos integrados con `git subtree`:

**Traer cambios del Switch al monolito:**
```bash
git fetch switch
git subtree pull --prefix=backend/switch-pagos switch main
```

**Enviar cambios del monolito al Switch:**
```bash
git subtree push --prefix=backend/switch-pagos switch main
```

## 🏗 Arquitectura General

### Core de Cuentas (banquito-core)
- Gestión de clientes, cuentas y transacciones
- Motor transaccional atómico
- Consulta de saldos (disponible y contable)
- Validación de estados de cuenta

**Responsable inicial:** Equipo Core (Personas 1-3, 7)

### Switch de Pagos Masivos (switch-pagos)
- Procesamiento de archivos batch (línea por línea)
- Validación estructural y fraude operativo
- Cálculo de comisiones y tarifaje
- Notificaciones y reportes

**Responsable inicial:** Equipo Switch (Personas 4-6, 7)

## 📋 Requisitos Funcionales

Mapeo de RF (Requisitos Funcionales) en el código:

| RF | Módulo | Responsable |
|----|--------|-------------|
| RF-01 a RF-07 | Core | Equipo Core |
| RF-01 a RF-08 | Switch | Equipo Switch |
| Integración | Ambos | Persona 7 |

Ver detalles en [docs/REQUISITOS_RF.md](docs/REQUISITOS_RF.md).

## 🔐 Seguridad

- Los POMs no deben ser modificados sin consenso
- Las estructuras internas de `backend/banquito-core/src` y `backend/switch-pagos/src` son autónomas
- Cambios arquitectónicos: crear issue/discussion

## 📞 Contacto

Equipo de desarrollo — Banco BanQuito 2026

---

**Última actualización:** 6 de mayo de 2026
