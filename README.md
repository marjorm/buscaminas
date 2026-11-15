# Buscaminas — Examen 2 de Programación 4

Juego web multijugador por cuentas, con tres niveles, tablero, banderas, historial y mejores tiempos. El juego corre en **una sola aplicación** en `http://localhost:8080`.

## Requisitos

- Java JDK 17.
- MySQL Server 8, activo y con usuario que pueda crear la base `minas`.
- Conexión a Internet para obtener las dependencias de Maven y cargar React 18 y Babel desde CDN.
- Navegador moderno. **No se necesita Node.js, npm, Vite ni otro servidor frontend**.

## 1. Crear la base de datos desde cero

**ADVERTENCIA:** `minas.sql` elimina las tablas `usuario` y `partida` de `minas` antes de recrearlas. **No lo vuelvas a importar si deseas conservar partidas.** Haz un respaldo primero.

En Windows PowerShell, desde la raíz del proyecto:

```powershell
$mysql = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
$rutaSQL = (Resolve-Path .\minas.sql).Path.Replace('\', '/')
& $mysql -h 127.0.0.1 -P 3306 -u root -p -e "SOURCE $rutaSQL"
```

El esquema contiene exactamente las tablas `usuario` y `partida`, y la vista `vw_ranking`. Usa `utf8mb4`, claves foráneas con borrado en cascada, `CHECK`, JSON, índices y control de versiones de partidas.

### Datos iniciales de demostración

| Usuario | Contraseña | Descripción |
|---|---|---|
| `jugador1` | `Prueba123$` | Cuenta de prueba |
| `docente` | `Prueba123$` | Cuenta de prueba, administrador |

El archivo `minas.sql` incorpora **una partida de ejemplo ganada por `jugador1` de 45.000 ms**, fechada el 10 de noviembre de 2026. **Es un dato precargado**, no una partida real de quien ingrese con esa cuenta. El ranking la incluye por diseño. Las partidas nuevas ganadas se incorporan al ranking y las derrotas se excluyen. El script se mantiene conforme al material inicial del examen.

## 2. Configurar y ejecutar

En PowerShell, desde la raíz (donde está `pom.xml`):

```powershell
$env:DB_PORT = '3306'
$env:DB_PASSWORD = Read-Host 'Contraseña de MySQL'
& .\mvnw.cmd clean verify
& .\mvnw.cmd spring-boot:run
```

Abrir `http://localhost:8080`. El frontend está en `src/main/resources/static/`, servido por Spring Boot; se carga React 18 con JSX desde CDN y utiliza únicamente `fetch` para acceder a la API.

Para detener el servidor usa `Ctrl+C`. La contraseña se pasa por variable de entorno en la sesión actual; **no la incluyas en Git**. Las propiedades de `src/main/resources/application.properties` conservan la conexión a `minas` y usan `ddl-auto=validate` para no reconstruir el esquema al arrancar.

## 3. Operaciones y reglas

| Método | Ruta | Propósito |
|---|---|---|
| POST | `/api/auth/registro` | Registro (201, 400, 409) |
| POST | `/api/auth/login` | Login (200, 401) |
| POST | `/api/auth/logout` | Logout (204) |
| GET | `/api/auth/me` | Sesión actual |
| GET | `/api/auth/csrf` | Token anti-CSRF |
| GET | `/api/niveles` | Catálogo con los tres niveles |
| POST | `/api/partidas` | Crear partida (201, 409 si ya existe una activa) |
| GET | `/api/partidas/actual` | Recuperar partida en curso (200, 404) |
| POST | `/api/partidas/{id}/acciones` | Revelar casilla o alternar bandera |
| GET | `/api/partidas/{id}` | Consultar partida terminada |
| POST | `/api/partidas/{id}/abandonar` | Abandonar partida (operación adicional) |
| GET | `/api/usuarios/{id}/partidas` | Historial del jugador |
| GET | `/api/ranking` | Mejores tiempos por nivel |

Niveles: **Principiante** 9×9 con 10 minas; **Intermedio** 16×16 con 40 minas; **Experto** 16×30 con 99 minas.

Ejemplo de solicitud para crear una partida, después de iniciar sesión:

```json
{"nivel":"PRINCIPIANTE"}
```

Ejemplo de acción (coordenadas desde 0):

```json
{"tipo":"REVELAR","fila":0,"columna":0}
```

Para colocar o retirar una bandera, usa `"tipo":"BANDERA"`. Las peticiones POST requieren sesión y token `X-XSRF-TOKEN` obtenido de `/api/auth/csrf`.

El servidor crea las minas con aleatoriedad segura, cuenta adyacencias, abre en cadena los ceros, detecta victoria al abrir todas las casillas seguras y derrota al pulsar una mina. Todo el tablero se persiste en la columna JSON de MySQL; **el cliente solo recibe casillas visibles**, nunca la lista de minas ocultas. El reloj animado del navegador es visual; el tiempo final oficial lo fija el servidor en milisegundos.

El historial distingue `GANADA`, `PERDIDA`, `ABANDONADA` y `EN_CURSO`. El ranking de la vista SQL usa `MIN`, `COUNT`, `AVG`, `JOIN` y `GROUP BY`, y cuenta únicamente victorias; las partidas perdidas no aparecen. La interfaz calcula y destaca la posición del usuario actual dentro del nivel seleccionado.

## 4. Seguridad y validaciones

- Autenticación por sesión HTTP con Spring Security.
- Hash de contraseñas con `Pbkdf2PasswordEncoder`, PBKDF2-HMAC-SHA256, 16 bytes de sal y 600.000 iteraciones; almacenamiento hexadecimal de 96 caracteres.
- Registro con validación del lado del servidor: nombre, usuario, correo, contraseña y confirmación.
- Solo el propietario (o el administrador en el historial) puede acceder a sus recursos.
- Acceso no autenticado 401; recurso ajeno 403; partida duplicada 409.
- Las validaciones y errores de la API tienen cuerpo JSON sin detalles internos.

## 5. Pruebas y demostración

```powershell
$env:DB_PASSWORD = Read-Host 'Contraseña de MySQL'
& .\mvnw.cmd clean verify
& .\mvnw.cmd '-Dtest=MotorBuscaminasTest' test
```

La validación anterior a la revisión final pasó **20/20 pruebas** y comprobó con MySQL real registro, login, niveles, creación, banderas, revelado, 401/403/409, historial, ranking y persistencia tras reinicio. Repetir `clean verify` tras cualquier modificación.

Probar en el navegador ambos usuarios semilla y un usuario registrado desde la aplicación. Verificar también partidas ganadas y perdidas, recarga de la página, cierre de sesión, cronómetro y tabla de posiciones. El ranking contiene la partida precargada de 45 segundos descrita arriba.

## 6. Proyecto y control de versiones

Estructura principal:

```text
pom.xml
mvnw.cmd
minas.sql
src/main/java/com/bachillerato/buscaminas/
  config/         (seguridad y codificador)
  controllers/    (API y errores)
  dto/            (solicitudes y respuestas)
  game/           (motor y niveles)
  models/         (entidades JPA)
  repositories/   (consultas JPQL y SQL)
  services/       (reglas de negocio y transacciones)
src/main/resources/static/   (interfaz web)
src/test/java/              (pruebas JUnit)
```

La rama `feature/datos-seguridad` comprende entidades, repositorios, autenticación, historial y ranking. La rama `feature/juego-react` incorpora motor, API de juego, interfaz React y pruebas, junto con ajustes de integración. El trabajo final se fusiona a `main` mediante Pull Request después de ejecutar las pruebas.

Para entregar, incluir la URL pública del repositorio y `Grupo@examen2.zip` que contenga la carpeta `examen2-respuesta/`, **incluyendo `.git/`** y excluyendo `target/`, `build/`, `.idea/` y archivos `.iml`.
