# Deploy to Yandex Cloud VM with Docker Compose

## 1) Fix Docker package conflict on VM

```bash
sudo apt-get remove -y docker-compose-v2
sudo apt-get -f install -y
sudo apt-get install -y docker-compose-plugin
sudo systemctl daemon-reload
sudo systemctl restart docker
sudo systemctl status docker --no-pager
```

If Docker still fails, inspect logs:

```bash
journalctl -xeu docker.service --no-pager | tail -n 80
```

## 2) Copy project to VM

From local machine (PowerShell):

```powershell
scp -r "C:\Users\Katacan\Documents\RegistrationOffice" nabotov@178.154.216.17:/home/nabotov/
```

On VM:

```bash
cd /home/nabotov/RegistrationOffice
cp .env.example .env
```

Edit `.env`:

```bash
nano .env
```

Use values:

```dotenv
DB_NAME=registration_office
DB_USER=registration_user
DB_PASS=<your_db_password>
JWT_SECRET=<your_jwt_secret>
```

## 3) Start containers

```bash
cd /home/nabotov/RegistrationOffice
sudo docker compose up -d --build
sudo docker compose ps
```

## 4) Check logs and health

```bash
sudo docker compose logs -f backend
```

Open API by IP:

- `http://178.154.216.17/api/v1/login`
- `http://178.154.216.17/api/v1/registration`

## 5) Useful commands

Stop:

```bash
sudo docker compose down
```

Restart:

```bash
sudo docker compose up -d
```

Rebuild backend after code changes:

```bash
sudo docker compose up -d --build backend
```

