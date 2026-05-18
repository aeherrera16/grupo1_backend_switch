# BancoBanQuito — Switch de Pagos

Servicio encargado de procesar los lotes de pagos masivos enviados por las empresas. Recibe archivos CSV a través de la interfaz web o vía SFTP, valida cada registro, debita la cuenta origen y acredita las cuentas destino en el Core bancario. Al finalizar el procesamiento envía notificaciones por correo electrónico a la empresa.

Desarrollado en **Java** con **Spring Boot**. Usa **PostgreSQL** como base de datos.
