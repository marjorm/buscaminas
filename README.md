# Buscaminas — Examen 2 de Programación 4

Aplicación web de Buscaminas con cuentas de usuario, tres niveles, partidas persistentes, historial y ranking. **Backend y frontend se ejecutan juntos** mediante Spring Boot en `http://localhost:8080`.

## Integrantes 	Grupo5Exa2
- Maria Ramirez Elizondo
- Cristopher Madrigal Masis

## 1. Requisitos

- **JDK 17**, **MySQL Server 8** y un navegador moderno.
- Conexión a Internet para descargar dependencias de Maven y cargar **React 18 y Babel standalone** desde CDN.
- MySQL con una cuenta que pueda crear el esquema `minas` (la configuración suministrada utiliza `root`).
- **No se requiere Node.js, npm, Vite ni un servidor frontend separado.**
- Se incluye Maven Wrapper (`mvnw.cmd`); no es obligatorio instalar Maven globalmente.

## 2. Base de datos MySQL

El archivo `minas.sql` crea el esquema `minas`, las tablas `usuario` y `partida`, y la vista `vw_ranking`. Incluye claves foráneas, índices, restricciones, JSON y control de versión de partidas.

> **Advertencia:** el script elimina y vuelve a crear tablas y vista. **Importarlo de nuevo puede borrar usuarios y partidas existentes.** Úsalo solo para preparar una base nueva o después de hacer un respaldo.

### Opción A: MySQL Workbench

1. Iniciar el servicio de MySQL 8 y conectarse a la instancia.
2. Abrir `minas.sql` con **File > Open SQL Script**.
3. Ejecutar el script completo con el botón del rayo.
4. Actualizar **Schemas** y comprobar que existe `minas`, con `usuario`, `partida` y `vw_ranking`.

### Opción B: PowerShell (Windows)

Desde la carpeta del proyecto (donde están `pom.xml` y `minas.sql`):

```powershell
$mysql = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
$rutaSQL = (Resolve-Path .\minas.sql).Path.Replace('\', '/')
& $mysql -h 127.0.0.1 -P 3306 -u root -p -e "SOURCE $rutaSQL"
```

**Ajusta `-P 3306` al puerto real de tu servidor** (por ejemplo, `3307`). Si MySQL está instalado en otra carpeta, ajusta `$mysql`.


## 3. Configuración y ejecución

Abrir **PowerShell** en la raíz del proyecto. Iniciar MySQL 8 y configurar las variables en **esa misma terminal**:

```powershell
$env:DB_PORT = '3306'
$env:DB_PASSWORD = Read-Host 'Contraseña de MySQL'
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Si el servidor escucha en **3307**, cambiar la primera línea por `$env:DB_PORT = '3307'`. El puerto por defecto de la aplicación es `8080`.

Abrir **http://localhost:8080**. Detener el servidor con `Ctrl+C`.

El archivo `src/main/resources/application.properties` utiliza:

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:${DB_PORT:3306}/minas
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
```

La contraseña **no debe escribirse en el repositorio**. `ddl-auto=validate` valida las tablas existentes; **no crea el esquema**, por lo que primero se debe importar `minas.sql`.

Si `.\mvnw.cmd` no funciona, puede utilizarse `mvn` con Maven instalado. Las pruebas de contexto requieren MySQL accesible y las variables correctas.

## 4. Uso del juego

1. Abrir la página y registrarse, o iniciar sesión con una cuenta de demostración.
2. Elegir un nivel:

   | Nivel | Dimensiones | Minas |
   |---|---|---:|
   | PRINCIPIANTE | 9 × 9 | 10 |
   | INTERMEDIO | 16 × 16 | 40 |
   | EXPERTO | 16 × 30 | 99 |

3. **Clic izquierdo:** revelar una casilla. **Clic derecho:** colocar o retirar bandera. También existe un botón para activar el modo bandera.
4. Se gana al descubrir todas las casillas seguras y se pierde al descubrir una mina. Las casillas sin minas adyacentes permiten apertura en cadena.
5. La partida en curso se guarda en MySQL y puede recuperarse al recargar la página. También existe la opción de abandonar.
6. Consultar **Historial** para ver las partidas del usuario y **Ranking** para consultar mejores tiempos por nivel.

El tablero muestra estados **`OCULTA`**, **`ABIERTA`**, **`BANDERA`** y **`EXPLOTADA`**. El servidor genera las minas mediante `SecureRandom`, calcula adyacencias, valida acciones y determina victoria o derrota. El JSON interno persistido en MySQL contiene la información del tablero, pero **no se envía completo al navegador**: las respuestas muestran únicamente la información visible. El cronómetro del navegador es visual; el tiempo final oficial se calcula en el servidor.

En la base de datos, `estado = TERMINADA` puede corresponder tanto a victoria como a derrota. El campo `resultado` dentro del JSON del tablero diferencia **`GANADA`** y **`PERDIDA`**. El ranking considera únicamente las victorias; las derrotas no se utilizan como mejores tiempos.

## 5. API REST

| Método | Endpoint | Función |
|---|---|---|
| POST | `/api/auth/registro` | Registrar usuario |
| POST | `/api/auth/login` | Iniciar sesión |
| POST | `/api/auth/logout` | Cerrar sesión |
| GET | `/api/auth/me` | Consultar usuario autenticado |
| GET | `/api/auth/csrf` | Obtener token CSRF |
| GET | `/api/niveles` | Consultar niveles |
| POST | `/api/partidas` | Crear partida |
| GET | `/api/partidas/actual` | Recuperar partida activa |
| POST | `/api/partidas/{id}/acciones` | Revelar o marcar casilla |
| GET | `/api/partidas/{id}` | Consultar partida terminada |
| POST | `/api/partidas/{id}/abandonar` | Abandonar partida (función adicional) |
| GET | `/api/usuarios/{id}/partidas` | Historial de usuario |
| GET | `/api/ranking` | Mejores tiempos por nivel |



## 6. Seguridad y validaciones

- Sesiones HTTP administradas con Spring Security.
- Contraseñas almacenadas mediante **PBKDF2-HMAC-SHA256**, con sal de 16 bytes y 600 000 iteraciones; representación hexadecimal de 96 caracteres.
- Validación de nombre, nombre de usuario, correo, contraseña y confirmación en el registro.
- Restricción de acceso a las partidas del propietario; el historial permite acceso del propio usuario y del administrador.
- Manejo de errores mediante respuestas HTTP y JSON: por ejemplo, **401** sin autenticación, **403** sin autorización y **409** ante conflictos como partida activa o usuario duplicado.
- El cliente no recibe el hash de contraseña ni la ubicación de minas ocultas.

## 7. Pruebas

Desde PowerShell, con MySQL iniciado y `DB_PASSWORD` / `DB_PORT` configuradas:

```powershell
.\mvnw.cmd clean test
```

Para ejecutar solo las pruebas del motor:

```powershell
.\mvnw.cmd '-Dtest=MotorBuscaminasTest' test
```

**Última ejecución completa comunicada (15 de noviembre de 2026): 22 pruebas, 0 fallos, 0 errores y 0 omitidas (`BUILD SUCCESS`).**

| Suite | Pruebas |
|---|---:|
| `BuscaminasApplicationTests` | 1 |
| `MotorBuscaminasTest` | 8 |
| `AuthServiceTest` | 5 |
| `HistorialServiceTest` | 3 |
| `PasswordServiceTest` | 5 |
| **Total** | **22** |

Las pruebas cubren el contexto Spring Boot, reglas del motor (incluyendo apertura en cadena, banderas, victoria, derrota y protección de casillas ocultas), autenticación, historial y contraseñas. **No sustituyen todas las pruebas manuales de integración de los endpoints y del navegador.**

### Verificación manual recomendada

- Registrar una cuenta, iniciar sesión, consultar el perfil y cerrar sesión.
- Crear una partida en cada nivel; revelar casillas y colocar/quitar banderas.
- Verificar el rechazo de una segunda partida activa (`409`).
- Recargar la página y comprobar que el tablero, banderas y movimientos se conservan.
- Comprobar una victoria, una derrota y un abandono.
- Consultar historial y ranking; comprobar que las derrotas no se cuentan como mejores tiempos.
- Intentar acceder sin sesión o a recursos de otro usuario y comprobar los códigos de error.
- Comprobar que las respuestas de la API no revelan minas ocultas.

## 8. Organización del proyecto

```text
pom.xml
mvnw.cmd
minas.sql
README.md
src/
  main/
    java/com/bachillerato/buscaminas/
      config/          Seguridad y codificación de contraseñas
      controllers/     Endpoints REST y manejo de errores
      dto/             Solicitudes y respuestas
      game/            Motor y niveles
      models/          Entidades JPA
      repositories/    Consultas a MySQL
      services/        Reglas de negocio y transacciones
    resources/
      application.properties
      static/
        index.html
        aplicacion.jsx
        estilos.css
  test/java/           Pruebas JUnit y Mockito
```

### Trabajo en equipo y Git

- `feature/datos-seguridad`: modelo de datos, repositorios, autenticación, historial, ranking y pruebas.
- `feature/juego-react`: motor del juego, API de partidas, interfaz React y pruebas, además de ajustes de integración.
- Los cambios se integraron a `main` mediante Pull Requests. El historial completo está en `.git/`.

**Repositorio:** https://github.com/marjorm/buscaminas


