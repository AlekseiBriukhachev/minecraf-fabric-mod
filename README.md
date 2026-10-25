# 🚀 Message Mod

Message Mod is a Fabric mod for Minecraft that lets players send messages to a PostgreSQL database.

This project is built for people who want to use the mod, not for developers working on it. In normal use, you only need to run a few commands and start the game.

## What the mod does

When you press `M` in-game, your message is sent to the server and stored in PostgreSQL.

## Requirements

Before you start, make sure you have:
- Java 21+
- Docker Desktop or Docker Engine with Compose
- Git

## Quick start

### 1) Download the project

```bash
git clone https://github.com/AlekseiBriukhachev/minecraf-fabric-mod.git
cd minecraf-fabric-mod
```

### 2) Create your local database config

Create a file named `.env` in the project root with your own local values.

Example:

```env
POSTGRES_DB=minecraft
POSTGRES_USER=mcuser
POSTGRES_PASSWORD=changeme
DB_URL=jdbc:postgresql://localhost:5432/minecraft
DB_USERNAME=mcuser
DB_PASSWORD=changeme
```

Important:
- use your own local values
- do not commit this file
- never share real credentials in a public repository
- the file is only for local testing

### 3) Start the database

```bash
docker compose up -d
```

### 4) Accept the Minecraft EULA

Open `run/eula.txt` and change:

```txt
eula=false
```

to:

```txt
eula=true
```

### 5) Start the mod

Run the server:

```bash
./gradlew runServer
```

Then, in another terminal, start the client:

```bash
./gradlew runClient
```

## How to use it

Once the game starts:

1. Press `M`
2. Type your message
3. Click `Send`

Your message is stored in PostgreSQL.

## View saved messages

```bash
docker exec -it minecraft-postgres psql -U mcuser -d minecraft -c "SELECT * FROM messages;"
```

## Useful commands

```bash
# Start PostgreSQL
docker compose up -d

# Stop PostgreSQL
docker compose down

# View messages
docker exec -it minecraft-postgres psql -U mcuser -d minecraft -c "SELECT * FROM messages;"
```

## Troubleshooting

### Missing DB config

If you see:

```text
Missing required config: DB_URL
```

make sure `.env` exists in the project root and contains your own local values.

### Docker is not running

Start Docker Desktop or the Docker daemon, then run:

```bash
docker compose up -d
```

### The server does not start

Make sure `run/eula.txt` is set to `eula=true`.

## Notes

This project is meant for local use and testing only.

For normal use, you do not need to manually generate classes, initialize Gradle wrapper files, or change IDE settings.

Do not commit real database credentials. Keep them local only.
