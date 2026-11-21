# Despliegue en EC2 (AWS Academy Learner Lab)

El Learner Lab entrega credenciales temporales, por lo que el despliegue se hace
a mano con el artefacto que genera GitHub Actions.

## 1. Descargar el artefacto

En GitHub: **Actions → CI → (ejecución) → Artifacts → `los-maestros-backend-<sha>`**,
o con la CLI:

```bash
gh run download --repo diegamigo-code/backend_ferreteria --dir artefacto
```

El artefacto trae `app.jar`, `app.jar.sha256`, `app.env.example`,
`los-maestros-backend.service`, `Caddyfile`, `los-maestros-caddy.service`, este `DEPLOY.md` y `BUILD_INFO.txt`.

## 2. Preparar la EC2 (una sola vez)

- AMI: Amazon Linux 2023, tipo `t2.micro`/`t3.micro`, key pair `vockey` (o la tuya).
- Security group: entrada TCP **22** (tu IP), **80** y **443** (0.0.0.0/0). El 80 lo usa Let's Encrypt para validar el certificado.
  El **8080** solo hace falta para probar antes de tener HTTPS.

### Elastic IP (para que la IP no cambie al reiniciar el laboratorio)

```bash
ID_EC2=<id-de-la-instancia>
ASIGNACION=$(aws ec2 allocate-address --domain vpc --query AllocationId --output text)
aws ec2 associate-address --instance-id $ID_EC2 --allocation-id $ASIGNACION
aws ec2 describe-addresses --allocation-ids $ASIGNACION --query 'Addresses[0].PublicIp' --output text
```

Con esa IP el dominio HTTPS queda como `54-12-34-56.sslip.io` (la IP con guiones). `sslip.io`
resuelve ese nombre a la IP, así que no hace falta comprar un dominio.

```bash
sudo dnf install -y java-21-amazon-corretto-headless
sudo mkdir -p /opt/los-maestros-backend && sudo chown ec2-user: /opt/los-maestros-backend
```

### Base de datos

- **RDS (MySQL)**: en el security group de RDS, permite TCP 3306 desde el security group de la EC2.
  `DB_HOST` es el endpoint de RDS.
- **MySQL en la misma EC2**: `sudo dnf install -y mariadb105-server && sudo systemctl enable --now mariadb`,
  crea el usuario y usa `DB_HOST=localhost`.

La base `los_maestros` y sus tablas se crean solas al primer arranque.

## 3. Copiar y configurar

Desde tu equipo (con `labsuser.pem` descargado de *AWS Details*):

```bash
cd artefacto/los-maestros-backend-*/
sha256sum -c app.jar.sha256
scp -i labsuser.pem app.jar app.env.example los-maestros-backend.service Caddyfile los-maestros-caddy.service \
    ec2-user@<IP-PUBLICA>:/opt/los-maestros-backend/
```

En la EC2:

```bash
cd /opt/los-maestros-backend
cp app.env.example app.env && nano app.env          # completar variables
chmod 600 app.env
sudo cp los-maestros-backend.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now los-maestros-backend
curl http://localhost:8080/actuator/health          # {"status":"UP"}
```

### HTTPS con Caddy

Caddy recibe en el 443, saca solo el certificado de Let's Encrypt para `DOMINIO` y le pasa todo
al backend en el 8080. Con HTTPS el frontend funciona tanto en S3 como en GitHub Pages.

```bash
curl -fL "https://caddyserver.com/api/download?os=linux&arch=amd64" -o caddy
sudo install -m 755 caddy /usr/local/bin/caddy
sudo cp /opt/los-maestros-backend/los-maestros-caddy.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now los-maestros-caddy
curl https://<ip-con-guiones>.sslip.io/actuator/health   # {"status":"UP"}
```

`DOMINIO` se lee de `app.env`. Logs: `journalctl -u los-maestros-caddy -f`.
Si Let's Encrypt rechaza el certificado por límite de solicitudes del dominio `sslip.io`,
se puede probar con `nip.io` (`54-12-34-56.nip.io`) cambiando `DOMINIO`.

- Login: `POST https://<DOMINIO>/api/auth/login` con el `ADMIN_EMAIL` y `ADMIN_PASSWORD` de `app.env`
- API: `https://<DOMINIO>/api/productos`
- Swagger: `https://<DOMINIO>/swagger-ui.html`

## 4. Primer uso

1. Entra al sitio con `ADMIN_EMAIL` / `ADMIN_PASSWORD`: el catálogo y el stock ya vienen cargados.
2. Los contratistas se registran marcando "Soy contratista"; en **Panel → Usuarios** se les asigna el cupo de crédito.
3. Los pagos de facturas a crédito se registran en **Panel → Pedidos → Registrar pago**.

El frontend se publica en un bucket S3 o en GitHub Pages; los pasos están en el README del repo
del frontend (sección *Despliegue en AWS*). Las URLs del frontend van en `CORS_ORIGIN`, y
`https://<DOMINIO>` va en el `config.js` del frontend.

## 5. Actualizar a una nueva versión

```bash
scp -i labsuser.pem app.jar ec2-user@<IP-PUBLICA>:/opt/los-maestros-backend/
ssh -i labsuser.pem ec2-user@<IP-PUBLICA> 'sudo systemctl restart los-maestros-backend'
```

## Con Docker (alternativa)

```bash
docker build -t los-maestros-backend .
docker run -d -p 8080:8080 --env-file app.env los-maestros-backend
```

> Con la Elastic IP la dirección no cambia al reiniciar el laboratorio. Si igual cambia,
> se actualiza `DOMINIO` en `app.env`, se reinicia Caddy y se cambia `config.js` del frontend
> (no hace falta recompilar).
> Logs: `journalctl -u los-maestros-backend -f`.
