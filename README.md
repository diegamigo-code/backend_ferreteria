# backend_ferreteria
Hola, esto es un backend

frontend: https://github.com/tomihm/Ferreteria.git.io

API REST de la ferretería Los Maestros (Spring Boot 4, Java 21). El proyecto está en `los_maestros/`.

| Endpoint | Descripción |
| --- | --- |
| `/api/auth/login`, `/api/auth/registro`, `/api/auth/me` | Login con JWT, registro de clientes y datos del usuario actual |
| `/api/productos` | Productos (CRUD por SKU, filtros por categoría y marca) |
| `/api/inventario` | Stock por producto (consulta pública), entradas, salidas y bajo stock |
| `/api/usuarios` | Usuarios (RUT, teléfono y rol) |
| `/api/facturas` | Emitir, consultar y anular facturas (descuenta y repone stock), estado del pedido y pago |
| `/api/facturas/mias` | Pedidos del usuario que inició sesión |
| `/api/credito` | Cupo, saldo y movimientos del crédito a 30 días de los contratistas |
| `/api/mensajes` | Formulario de contacto (envío público, lectura solo admin) |
| `/api/auditoria` | Historial de cambios |
| `/actuator/health` | Estado de la aplicación |
| `/swagger-ui.html` | Documentación interactiva |

## Autenticación (JWT)

1. `POST /api/auth/login` con `{"email":"...","password":"..."}` devuelve `{"token":"...","tipo":"Bearer",...}`.
2. En cada petición protegida se envía el header `Authorization: Bearer <token>`.
3. El token dura 60 minutos (`JWT_EXPIRACION_MINUTOS`).

| Rol | Puede |
| --- | --- |
| Público (sin token) | Ver productos y stock, registrarse, iniciar sesión, enviar mensajes de contacto, `/actuator/health`, Swagger |
| `CLIENTE` | Lo anterior, `GET /api/auth/me`, comprar (`POST /api/facturas`) y ver sus pedidos (`GET /api/facturas/mias`) |
| `CONTRATISTA` | Lo mismo que el cliente, y comprar con `"formaPago":"CREDITO"` hasta su cupo; ve su cuenta en `GET /api/credito/mio` |
| `VENDEDOR` | Lo anterior, todas las facturas y movimientos de inventario (entrada/salida, consultas) |
| `ADMIN` | Todo: productos, usuarios, auditoría, inventario, cupos de crédito, pagos y mensajes |

`POST /api/auth/registro` crea un `CLIENTE`, o un `CONTRATISTA` si se envía `"rol":"CONTRATISTA"` (sin cupo hasta que el admin lo asigne con `PUT /api/credito/{usuarioId}`). Los vendedores y otros administradores los crea un `ADMIN` en `POST /api/usuarios` indicando `"rol"`.
Cuando un cliente o contratista emite una factura, el `usuarioId` se toma del token, no del body.

## Datos iniciales

Al primer arranque, si la tabla de productos está vacía, se cargan los 33 productos del catálogo con su stock desde `src/main/resources/catalogo-inicial.json`. Se desactiva con `app.catalogo.cargar-inicial=false`.
Al arrancar, si no hay ningún `ADMIN`, se crea uno con `ADMIN_EMAIL` / `ADMIN_PASSWORD`
(en el perfil por defecto: `admin@losmaestros.cl` / `admin12345`).

En Swagger, usa el botón **Authorize** y pega el token.

## Perfiles

- **default**: base H2 en memoria. `./mvnw spring-boot:run` desde `los_maestros/`.
- **prod**: MySQL. Requiere las variables `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `CORS_ORIGIN`, `JWT_SECRET`, `ADMIN_EMAIL` y `ADMIN_PASSWORD`.

```bash
SPRING_PROFILES_ACTIVE=prod DB_HOST=localhost DB_NAME=los_maestros DB_USER=root \
DB_PASSWORD=secreto CORS_ORIGIN=http://localhost:5173 \
JWT_SECRET=una-clave-de-al-menos-32-caracteres-123 ADMIN_EMAIL=admin@losmaestros.cl \
ADMIN_PASSWORD=admin12345 java -jar target/app.jar
```

## Empaquetado

```bash
cd los_maestros
./mvnw clean package -DskipTests   # genera target/app.jar
docker build -t los-maestros-backend .
```

## CI y despliegue en EC2

El workflow `.github/workflows/ci.yml` se ejecuta en cada push/PR a `main` que toque `los_maestros/`
(o a mano con *Run workflow*): compila con Java 21, corre las pruebas y publica el artefacto
`los-maestros-backend-<sha>` con `app.jar`, su checksum, un `app.env.example` y la unidad systemd
`los-maestros-backend.service`.

Los pasos para instalarlo en una EC2 del AWS Academy Learner Lab están en
[`los_maestros/deploy/DEPLOY.md`](los_maestros/deploy/DEPLOY.md).
