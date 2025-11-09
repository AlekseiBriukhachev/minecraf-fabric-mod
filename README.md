# 🚀 Пошаговая инструкция запуска проекта

## Шаг 1: Клонировать репозиторий

```bash
git clone https://github.com/AlekseiBriukhachev/minecraf-fabric-mod.git
```

## Шаг 2: Инициализация Gradle Wrapper

```bash
# Если у вас НЕТ установленного Gradle
# Скачайте gradle-wrapper.jar вручную и положите в gradle/wrapper/

# Если у вас ЕСТЬ Gradle (любая версия)
gradle wrapper --gradle-version 8.14
```

Или скачайте wrapper вручную:

```bash
cd gradle/wrapper
wget https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar
```

## Шаг 3: Проверка работы Gradle

```bash
./gradlew tasks
```

## Шаг 4: Генерация Protobuf классов

```bash
./gradlew generateProto
```
Ожидаемый результат:
```
BUILD SUCCESSFUL in Xs
```
Проверьте генерацию:
```bash 
ls -la build/generated/source/proto/main/java/com/example/messagemod/network/proto/
```
## Шаг 5: Настройка IDE (IntelliJ IDEA)

1. Откройте проект как Gradle проект
2. Подождите пока IDEA загрузит зависимости
3. Пометьте сгенерированные файлы как Source Root:

   - Правый клик на `build/generated/source/proto/main/java`
   - `Mark Directory as → Sources Root`

4. Invalidate Caches:

   - `File → Invalidate Caches → Invalidate and Restart`

## Шаг 6: Сборка проекта

```bash
./gradlew build
```
Если возникают ошибки:
### Ошибка: "Cannot resolve symbol MessageProto"
```bash
./gradlew clean
./gradlew generateProto
./gradlew build
```
Затем в IDEA: `File → Invalidate Caches → Restart`

### Ошибка: "Task :generateProto FAILED"

Проверьте:

1. Существует ли файл src/main/proto/message.proto?
2. Правильный ли синтаксис в .proto файле?

```bash
cat src/main/proto/message.proto
```
Должно быть:
```protobuf
syntax = "proto3";

option java_package = "com.example.messagemod.network.proto";
option java_outer_classname = "MessageProto";

message Message {
string text = 1;
}
```

### Ошибка с Fabric Loom
Если `fabric-loom 1.12-SNAPSHOT` не работает, измените в `gradle.properties`:

```gradle
loom_version=1.11-SNAPSHOT
```
Или используйте стабильную версию:

```gradle
loom_version=1.10.+
```

### Шаг 7: Настройка PostgreSQL

#### 1. Запустите PostgreSQL

```bash
docker-compose up -d
```

#### 2. Проверьте что работает

```bash
docker ps
```

Должно быть:

```docker
CONTAINER ID   IMAGE                PORTS                    NAMES
xxxxx          postgres:15-alpine   0.0.0.0:5432->5432/tcp   minecraft-postgres
```

#### 3. Запустите сервер

```bash
# Сначала примите EULA
# Откройте run/eula.txt и измените eula=false на eula=true

./gradlew runServer
```

#### 4. Запустите клиент (в другом терминале)

```bash
./gradlew runClient
```
Minecraft запустится в dev-режиме с модом.

#### 6. Тестируйте!

1. В игре нажмите M
2. Введите сообщение
3. Нажмите Send

#### 7. Проверьте базу данных
```bash
docker exec -it minecraft-postgres psql -U mcuser -d minecraft -c "SELECT * FROM messages;"
```
   Вы должны увидеть ваше сообщение! 🎉

Команды на каждый день:
```bash
# Запустить PostgreSQL
docker-compose up -d

# Остановить PostgreSQL
docker-compose down

# Посмотреть данные
docker exec -it minecraft-postgres psql -U mcuser -d minecraft -c "SELECT * FROM messages;"

# Очистить таблицу
docker exec -it minecraft-postgres psql -U mcuser -d minecraft -c "TRUNCATE TABLE messages;"
```

Готово! 🚀

PostgreSQL работает в Docker, сервер Minecraft подключён к базе данных, и вы можете отправлять сообщения через GUI!