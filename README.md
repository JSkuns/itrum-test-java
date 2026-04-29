# itrum-test-java (Wallet Management System)

REST API сервис для управления кошельками, разработанный с упором на высокую производительность и отказоустойчивость 
в условиях высокой конкуренции (до 1000 RPS на один кошелек).

## Технологический стек

| Категория             | Технология                           |
|-----------------------|--------------------------------------|
| **Язык**              | Java 17                              |
| **Фреймворк**         | Spring Boot 3.2.5                    |
| **База данных**       | PostgreSQL 15                        |
| **Миграции**          | Liquibase                            |
| **Транзакционность**  | Hibernate (JPA), Pessimistic Locking |
| **Повторные попытки** | Spring Retry + RetryTemplate         |
| **Контейнеризация**   | Docker, Docker Compose               |
| **Тестирование**      | JUnit 5, Mockito, MockMvc            |

## Требования к запуску

Для запуска проекта необходимо:
- **Docker** и **Docker Compose**
- **Java 17+** (для локальной сборки)

## Быстрый старт

### Сборка проекта
Сначала соберите JAR-файл приложения (пропуская тесты для скорости):
```bash
./mvnw clean package -DskipTests
```
### Запуск инфраструктуры
Запустите базу данных и приложение с помощью Docker Compose:
```bash
docker-compose up --build -d
```
### Проверка статуса
```bash
curl http://localhost:8080/actuator/health
```

## Документация API
### Пополнение или снятие средств
POST /api/v1/wallet/operations

Request Body:
```json
{
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "operationType": "DEPOSIT",
  "amount": 1000.50
}
```
operationType может быть DEPOSIT или WITHDRAW.

Success Response (200 OK):
```json
{
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "balance": 2500.50
}
```
### Получение баланса
GET /api/v1/wallets/{walletId}

Success Response (200 OK):
```json
{
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "balance": 2500.50
}
```

## Архитектурные решения
### Обработка конкурентных запросов (1000 RPS)
Для предотвращения потери данных и ошибок 500 Internal Server Error при одновременном доступе к одному кошельку 
реализована следующая стратегия:
#### Pessimistic Locking: 
Метод репозитория использует @Lock(LockModeType.PESSIMISTIC_WRITE), что генерирует SQL SELECT ... FOR UPDATE. Это 
гарантирует, что только одна транзакция может модифицировать баланс в конкретный момент времени.
#### Retry Mechanism: 
Используется RetryTemplate (Spring Retry). Если транзакция не может получить блокировку (таймаут) или происходит 
конфликт версий (OptimisticLockingFailureException), операция автоматически повторяется до 3 раз с экспоненциальной 
задержкой.
### Структура ошибок
Все ошибки возвращаются в едином формате ErrorResponse, что упрощает интеграцию на стороне клиента:
```json
{
  "status": 400,
  "error": "InsufficientFunds",
  "message": "Balance: 100, Requested: 200",
  "timestamp": "2026-04-28T12:00:00"
}
```

## Запуск нагрузочного теста в grafana k6:
```bash
# установка k6 в Windows (через Chocolatey), если необходимо
# choco install k6
# Запуск
k6 run load_test.js
```