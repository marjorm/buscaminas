
## Motor del juego y frontend React (integración del integrante 2)

La aplicación completa se ejecuta desde **una sola URL**: `http://localhost:8080`.
No se requiere instalar Node.js, npm ni ejecutar un servidor frontend independiente.
El navegador debe poder cargar los CDN de React 18 y Babel standalone (se necesita conexión a Internet durante la carga inicial de la página).

### Preparación desde cero en Windows

1. Instalar JDK 17 y MySQL 8.
2. Importar `minas.sql` en MySQL usando MySQL Workbench o el comando `mysql -u root -p -e "SOURCE C:/ruta/absoluta/minas.sql"`.
   **Advertencia:** el script reemplaza las tablas existentes. Respaldar los datos antes de volver a importarlo.
3. Abrir PowerShell en la raíz del repositorio (donde se encuentra `pom.xml`).
4. Si se usa el puerto 3306 de MySQL, establecer `$env:DB_PORT = '3306'` (si no se establece, se usa 3306).
5. Establecer la contraseña con `$env:DB_PASSWORD = Read-Host 'Contraseña de MySQL'` (no registrar contraseñas en Git).
6. Ejecutar `./mvnw.cmd clean test` y luego `./mvnw.cmd spring-boot:run`.
7. Abrir `http://localhost:8080` y registrar una cuenta nueva. El formulario pide nombre, nombre de usuario, correo, contraseña de ocho caracteres con una letra y un número, y confirmación.

**Credenciales de prueba**: el archivo original `minas.sql` incorpora los usuarios `docente` y `jugador1`, ambos con contraseña de prueba `Prueba123$` según su documentación. Se recomienda crear una cuenta nueva para probar el registro.

### Contrato de juego

- `GET /api/niveles` — catálogo de niveles.
- `POST /api/partidas` — `{ "nivel":"PRINCIPIANTE" }`; `201` o `409` si existe una activa.
- `GET /api/partidas/actual` — única partida activa; `404` si no hay.
- `POST /api/partidas/{id}/acciones` — `{ "tipo":"REVELAR", "fila":0, "columna":0 }` o `{ "tipo":"BANDERA", "fila":0, "columna":0 }`.
- `GET /api/partidas/{id}` — partida finalizada del usuario autenticado.
- `POST /api/partidas/{id}/abandonar` — operación adicional para terminar voluntariamente una partida y poder iniciar otra.
- `GET /api/usuarios/{id}/partidas` — historial del jugador autenticado.
- `GET /api/ranking` — ranking de mejores tiempos por usuario y nivel.

Las coordenadas son cero-indexadas. Las rutas `/api/**` requieren autenticación salvo registro, login y adquisición del token CSRF. Las solicitudes POST incluyen cookie de sesión y token `X-XSRF-TOKEN`, leído del endpoint `/api/auth/csrf`.

### Reglas, privacidad del tablero y tiempo

El backend genera minas aleatorias con `SecureRandom`, calcula adyacencias, abre en cadena zonas de valor cero, procesa banderas y detecta victorias o derrotas. El tablero persistido en la columna MySQL JSON incluye minas, banderas, casillas reveladas y el instante de creación en milisegundos. **Este documento JSON nunca se envía al navegador.** Las respuestas del juego incluyen únicamente un tablero filtrado con estados OCULTA, ABIERTA, BANDERA o EXPLOTADA y números de casillas reveladas; no incluyen coordenadas de minas ocultas. El cronómetro visual es aproximado; la duración final oficial se determina en el servidor.

La tabla `partida` conserva estado `TERMINADA` tanto para victorias como derrotas. Para distinguirlas sin agregar una tabla ni alterar el enum, el JSON conserva `resultado` = `GANADA` o `PERDIDA`. La vista `vw_ranking` filtra victorias y conserva compatibilidad con la partida semilla del SQL original (3 estrellas).

### Pruebas

`./mvnw.cmd -Dtest=MotorBuscaminasTest test` ejecuta pruebas unitarias de niveles, generación de minas, apertura en cadena, banderas, casos inválidos, derrota, victoria y protección de las casillas ocultas.

`./mvnw.cmd clean test` ejecuta todas las pruebas (requiere MySQL y variables de entorno configuradas, porque existe una prueba de contexto).

Se debe comprobar manualmente además: registro, login, logout, GET /api/auth/me, POST protegido sin sesión (401/403), creación de partida, 409 por doble partida activa, recuperación de partida tras reinicio, historial/ranking y que las respuestas REST no incluyen el arreglo `minas`.
