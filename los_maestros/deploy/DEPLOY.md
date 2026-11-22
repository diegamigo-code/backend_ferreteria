# Despliegue en Render

El backend se construye con el `Dockerfile` de `los_maestros/` en un Web Service gratis de Render.
La base es un MySQL gratis de Aiven, porque Render no ofrece MySQL. Render entrega una URL HTTPS
fija (`https://<servicio>.onrender.com`) y vuelve a desplegar con cada push a `main`.

## 1. Base MySQL en Aiven

1. En [aiven.io](https://aiven.io): **Create service → MySQL → Free plan**.
2. Cuando quede en *Running*, copiar de **Connection information**: host, puerto, usuario
   (`avnadmin`), contraseña y base (`defaultdb`). La conexión usa SSL y el driver lo activa solo.

## 2. Web Service en Render

1. En [render.com](https://render.com): **New → Web Service → `backend_ferreteria`**.
2. Configurar:
   - **Root Directory**: `los_maestros`
   - **Language**: Docker
   - **Instance Type**: Free
   - **Health Check Path** (en *Advanced*): `/actuator/health`
3. En **Environment Variables**:

```
SPRING_PROFILES_ACTIVE=prod
DB_HOST=<host-de-aiven>
DB_PORT=<puerto-de-aiven>
DB_NAME=defaultdb
DB_USER=avnadmin
DB_PASSWORD=<contraseña-de-aiven>
CORS_ORIGIN=https://tomihm.github.io
JWT_SECRET=<mínimo 32 caracteres aleatorios, ej: openssl rand -base64 48>
ADMIN_EMAIL=admin@losmaestros.cl
ADMIN_PASSWORD=<contraseña-del-admin>
```

`PORT` lo pone Render y la aplicación lo lee sola. Si el frontend se publica en otra dirección,
se agrega a `CORS_ORIGIN` separada por coma.

La primera compilación demora varios minutos porque descarga las dependencias de Maven.

## 3. Comprobar

```bash
URL=https://<servicio>.onrender.com
curl $URL/actuator/health
curl "$URL/api/productos?size=3"
curl -X POST $URL/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"admin@losmaestros.cl","password":"<contraseña-del-admin>"}'
```

## Primer uso

- En el primer arranque se crea el administrador de `ADMIN_EMAIL` y se carga el catálogo inicial
  (33 productos con su stock) si la tabla de productos está vacía.
- Los contratistas se registran desde el sitio; el administrador les asigna el cupo de crédito
  desde el panel.

## A tener en cuenta

- El plan gratis se apaga tras 15 minutos sin visitas y tarda cerca de un minuto en volver.
  Antes de una demostración conviene abrir `$URL/actuator/health` y esperar a que responda.
- Error de CORS en el navegador: la URL del sitio tiene que estar exacta en `CORS_ORIGIN`,
  sin `/` al final.
