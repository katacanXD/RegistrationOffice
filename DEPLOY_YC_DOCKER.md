# Deploy: VM + Docker Compose (RegistrationOffice)

Инструкция для деплоя backend на Linux VM (включая Yandex Cloud).

## 1. Подготовить сервер

Установите Docker Engine и Compose plugin (Ubuntu/Debian):

```bash
sudo apt-get update
sudo apt-get install -y docker.io docker-compose-plugin
sudo systemctl enable docker
sudo systemctl start docker
sudo systemctl status docker --no-pager
```

Если уже был старый пакет `docker-compose-v2` и есть конфликт:

```bash
sudo apt-get remove -y docker-compose-v2
sudo apt-get -f install -y
sudo apt-get install -y docker-compose-plugin
sudo systemctl restart docker
```

## 2. Скопировать проект на сервер

На локальной машине (PowerShell), подставьте свои значения:

```powershell
$PROJECT_PATH = "C:\Users\Katacan\Documents\RegistrationOffice"
$DEPLOY_USER = "<vm_user>"
$DEPLOY_HOST = "<vm_ip_or_domain>"
$DEPLOY_PATH = "/home/<vm_user>/RegistrationOffice"

scp -r $PROJECT_PATH "$DEPLOY_USER@$DEPLOY_HOST:$DEPLOY_PATH"
```

На сервере:

```bash
cd /home/<vm_user>/RegistrationOffice
cp .env.example .env
```

## 3. Настроить `.env`

Откройте файл:

```bash
nano .env
```

Минимальный набор:

```dotenv
DB_NAME=postgres
DB_USER=postgres
DB_PASS=<strong_password>
SECURITY_JWT_SECRET=<jwt_secret_min_32_chars>
APP_SECURITY_CORS_ALLOWED_ORIGINS=
APP_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS=
```

## 4. Запустить контейнеры

```bash
cd /home/<vm_user>/RegistrationOffice
sudo docker compose up -d --build
sudo docker compose ps
```

## 5. Проверить работоспособность

```bash
sudo docker compose logs --tail 200 backend
sudo docker compose logs --tail 100 nginx
```

API должно открываться по адресу:

- `http://<vm_ip_or_domain>/api/v1/auth/login`
- `http://<vm_ip_or_domain>/api/v1/auth/registration`

## 6. Операционные команды

Остановить:

```bash
sudo docker compose down
```

Перезапустить:

```bash
sudo docker compose up -d
```

Пересобрать только backend:

```bash
sudo docker compose up -d --build backend
```

## 7. Что проверить, если не работает

- Порт `80` открыт в security group / firewall
- Файл `deploy/nginx/default.conf` смонтирован в контейнер `nginx`
- `SECURITY_JWT_SECRET` задан и достаточно длинный
- БД поднята (`regoffice-postgres` в `healthy` статусе)
