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
`los-maestros-backend.service`, este `DEPLOY.md` y `BUILD_INFO.txt`.

## 2. Preparar la EC2 (una sola vez)

- AMI: Amazon Linux 2023, tipo `t2.micro`/`t3.micro`, key pair `vockey` (o la tuya).
- Security group: entrada TCP **22** (tu IP) y TCP **8080** (0.0.0.0/0 o el origen del frontend).

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
scp -i labsuser.pem app.jar app.env.example los-maestros-backend.service \
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

- API: `http://<IP-PUBLICA>:8080/api/productos`
- Swagger: `http://<IP-PUBLICA>:8080/swagger-ui.html`

## 4. Actualizar a una nueva versión

```bash
scp -i labsuser.pem app.jar ec2-user@<IP-PUBLICA>:/opt/los-maestros-backend/
ssh -i labsuser.pem ec2-user@<IP-PUBLICA> 'sudo systemctl restart los-maestros-backend'
```

## Con Docker (alternativa)

```bash
docker build -t los-maestros-backend .
docker run -d -p 8080:8080 --env-file app.env los-maestros-backend
```

> Al reiniciar el laboratorio la IP pública de la EC2 cambia: actualiza
> la URL de la API en el frontend (y recompila) o asigna una Elastic IP.
> Si el frontend se sirve por HTTPS (por ejemplo GitHub Pages), el navegador
> bloquea las llamadas a `http://<IP>:8080`; con el frontend en un bucket S3
> (website, `http://`) no hay ese problema.
> Logs: `journalctl -u los-maestros-backend -f`.
