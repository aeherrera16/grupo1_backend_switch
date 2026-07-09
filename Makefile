# --- COMANDOS DE INFRAESTRUCTURA HERRAMIENTAS (SONAR) ---
.PHONY: sonar-up sonar-down

sonar-up:
	@echo "Levantando SonarQube y Postgres..."
	docker compose -f docker-compose-sonar.yml up -d

sonar-down:
	@echo "Deteniendo SonarQube..."
	docker compose -f docker-compose-sonar.yml down