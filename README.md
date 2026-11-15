# Buscaminas

## 1. Descripción del proyecto

Buscaminas es una aplicación web desarrollada como parte de la evaluación del curso de Programación 4.

El proyecto consiste en implementar el juego Buscaminas mediante una arquitectura cliente-servidor, utilizando Spring Boot para el backend, MySQL para la persistencia de datos y React para la interfaz de usuario.

El sistema contempla el registro e inicio de sesión de jugadores, la administración de partidas, diferentes niveles de dificultad, el historial de partidas y un ranking de jugadores.

**Estado actual:** se encuentran implementados y probados los módulos de autenticación, persistencia, historial y ranking. La lógica del juego y la interfaz React están pendientes de integración.

## 2. Tecnologías utilizadas

- Java 17
- Spring Boot 3.3.4
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- MySQL 8
- Maven
- JUnit 5
- Mockito
- React 18 mediante CDN, previsto para la interfaz del juego

## 3. Requisitos previos

Para ejecutar el proyecto se necesita:

- JDK 17
- Maven 3.9 o una versión compatible
- MySQL 8
- Un navegador web
- IntelliJ IDEA u otro editor compatible con proyectos Maven

Se recomienda verificar las instalaciones mediante los comandos:

```bash
java -version
mvn -version
```

## 4. Configuración de la base de datos

El proyecto utiliza una base de datos MySQL llamada `minas`.

### 4.1. Crear la base de datos

Abrir MySQL Workbench e importar el archivo `minas.sql` proporcionado para el proyecto.

El script contiene las estructuras necesarias para almacenar usuarios y partidas, además de la vista utilizada para consultar el ranking.

**Importante:** el script puede eliminar y recrear estructuras existentes. No debe ejecutarse nuevamente sobre una base de datos que contenga información que se desee conservar.

### 4.2. Configurar la conexión

Abrir el archivo:

`src/main/resources/application.properties`

La configuración utilizada durante el desarrollo es:

```properties
spring.application.name=buscaminas
spring.datasource.url=jdbc:mysql://127.0.0.1:${DB_PORT:3306}/minas
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
```

El puerto `3307` corresponde al servidor MySQL utilizado durante el desarrollo. Si MySQL está configurado en otro puerto, debe ajustarse la URL de conexión.

La propiedad `ddl-auto=validate` permite verificar que las entidades Java coincidan con las tablas existentes sin solicitar a Hibernate que cree o modifique automáticamente el esquema.

## 5. Configurar la contraseña de MySQL

El puerto predeterminado de MySQL es 3306. Si la instalación utiliza otro puerto, se debe configurar la variable de entorno DB_PORT antes de ejecutar el proyecto.

Por ejemplo, para utilizar el puerto 3307 en PowerShell:
```powershell
$env:DB_PORT = "3307"
```

Por seguridad, la contraseña de la base de datos no se almacena directamente en `application.properties`.

En Windows PowerShell, ejecutar:

```powershell
$env:DB_PASSWORD = Read-Host "Introduce la contraseña de MySQL"
```

Ingresar la contraseña correspondiente al usuario configurado en MySQL.

Esta variable estará disponible en la sesión actual de PowerShell. Si se abre una nueva terminal, deberá configurarse nuevamente.

**No incluir contraseñas reales en el código ni subirlas a GitHub.**

## 6. Ejecutar el backend

Desde la carpeta principal del proyecto, donde se encuentra `pom.xml`, ejecutar:

```powershell
mvn spring-boot:run
```

Si la configuración es correcta, Spring Boot iniciará el servidor.

La dirección predeterminada del backend es:

`http://localhost:8080`

Para detener el servidor, presionar `Ctrl + C` en la terminal.

## 7. Autenticación y seguridad

El proyecto implementa autenticación mediante sesiones HTTP utilizando Spring Security.

Las contraseñas se almacenan mediante PBKDF2 con HMAC-SHA256, utilizando sal aleatoria.

Se implementaron las siguientes operaciones:

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/auth/registro` | Registrar un usuario |
| POST | `/api/auth/login` | Iniciar sesión |
| POST | `/api/auth/logout` | Cerrar sesión |
| GET | `/api/auth/me` | Consultar el usuario autenticado |
| GET | `/api/auth/csrf` | Obtener el token CSRF |

El sistema utiliza protección CSRF. Para realizar solicitudes POST desde un cliente HTTP, se debe obtener previamente el token CSRF y enviarlo mediante el encabezado `X-XSRF-TOKEN`, conservando las cookies de la sesión.

### 7.1. Ejemplo de registro

**POST** `/api/auth/registro`

```json
{
  "nombre": "Jugador Prueba",
  "nombreUsuario": "jugador_prueba",
  "email": "jugador@ejemplo.com",
  "password": "Clave12345"
}
```

### 7.2. Ejemplo de inicio de sesión

**POST** `/api/auth/login`

```json
{
  "nombreUsuario": "jugador_prueba",
  "password": "Clave12345"
}
```

Si las credenciales son correctas, el sistema crea una sesión autenticada.

## 8. Historial y ranking

El proyecto implementa consultas para recuperar las partidas de los jugadores y visualizar el ranking.

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/usuarios/{id}/partidas` | Consultar el historial del jugador |
| GET | `/api/ranking` | Consultar el ranking |

Estas operaciones requieren una sesión autenticada.

La consulta de historial comprueba que el usuario autenticado tenga permiso para consultar las partidas solicitadas.

El ranking utiliza la vista `vw_ranking` definida en la base de datos.

## 9. Pruebas automáticas

El proyecto utiliza JUnit 5 y Mockito para verificar el funcionamiento de los servicios.

Actualmente se han ejecutado correctamente las siguientes pruebas:

| Clase | Cantidad |
|---|---:|
| `PasswordServiceTest` | 3 |
| `AuthServiceTest` | 5 |
| `HistorialServiceTest` | 3 |
| `BuscaminasApplicationTests` | 1 |
| **Total** | **12** |

Las pruebas comprueban el funcionamiento del hash de contraseñas, la autenticación, la validación de usuarios duplicados, la recuperación del historial y el inicio del contexto de Spring Boot.

### 9.1. Ejecutar todas las pruebas

Primero configurar la variable `DB_PASSWORD` y comprobar que MySQL esté iniciado.

Después ejecutar:

```powershell
mvn clean test
```

El resultado obtenido durante el desarrollo fue:

```text
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 9.2. Ejecutar pruebas individuales

```powershell
mvn "-Dtest=PasswordServiceTest" test
mvn "-Dtest=AuthServiceTest" test
mvn "-Dtest=HistorialServiceTest" test
```

Las pruebas unitarias de estos servicios utilizan objetos simulados con Mockito, por lo que no requieren conexión directa a MySQL.

La prueba `BuscaminasApplicationTests` sí inicia el contexto de Spring Boot y utiliza la configuración de la base de datos.

## 10. Códigos de respuesta HTTP

El backend utiliza códigos HTTP para comunicar los resultados de las operaciones.

| Código | Significado |
|---|---|
| 200 | Solicitud procesada correctamente |
| 201 | Recurso creado correctamente |
| 204 | Operación completada sin contenido |
| 400 | Datos de solicitud inválidos |
| 401 | Usuario no autenticado o credenciales incorrectas |
| 403 | Acceso denegado |
| 404 | Recurso no encontrado |
| 409 | Conflicto, como un usuario o correo duplicado |

## 11. Módulos pendientes de integración

La versión final del proyecto incorporará:

- Lógica del juego Buscaminas ejecutada en el servidor.
- Niveles de dificultad.
- Creación y recuperación de partidas.
- Acciones para descubrir casillas y colocar banderas.
- Interfaz web desarrollada con React.
- Integración de la interfaz con la autenticación y los endpoints del backend.
- Pruebas finales del juego completo.

Esta sección se actualizará cuando se integren los módulos desarrollados por ambos integrantes.

## 12. Control de versiones

El proyecto utiliza Git y GitHub para organizar el trabajo colaborativo.

Durante el desarrollo se utilizan ramas separadas para los módulos de datos y seguridad y para la lógica del juego y la interfaz.

La integración final se realizará en la rama principal después de verificar que los componentes funcionan correctamente en conjunto.
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
