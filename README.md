# backend_ferreteria
Hola, esto es un backend

frontend: https://github.com/tomihm/Ferreteria.git.io

API REST de la ferretería Los Maestros (Spring Boot 4, Java 21). El proyecto está en `los_maestros/`.

| Endpoint | Descripción |
| --- | --- |
| `/api/productos` | Productos (CRUD por SKU, filtros por categoría y marca) |
| `/api/inventario` | Stock por producto, entradas, salidas y bajo stock |
| `/api/usuarios` | Clientes |
| `/api/facturas` | Emitir, consultar y anular facturas (descuenta y repone stock) |
| `/api/auditoria` | Historial de cambios |
| `/actuator/health` | Estado de la aplicación |
| `/swagger-ui.html` | Documentación interactiva |

## Perfiles

- **default**: base H2 en memoria. `./mvnw spring-boot:run` desde `los_maestros/`.
- **prod**: MySQL. Requiere las variables `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` y `CORS_ORIGIN`.

```bash
SPRING_PROFILES_ACTIVE=prod DB_HOST=localhost DB_NAME=los_maestros DB_USER=root \
DB_PASSWORD=secreto CORS_ORIGIN=http://localhost:5173 java -jar target/app.jar
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
