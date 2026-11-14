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