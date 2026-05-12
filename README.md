# webShopProject

Интернет-магазин одежды.

---

## О проекте

Полноценный сайт с:
- Каталогом товаров (одежда: категории, размеры, цвета, остатки)
- Корзиной
- Оформлением заказов
- Симуляцией оплаты
- Email-уведомлениями
- Админ-панелью

---

## Технологический стек

### Backend

| Технология | Версия | Назначение |
|------------|--------|------------|
| **Java** | 21 (LTS) | Основной язык, Virtual Threads |
| **Spring Boot** | 3.4.x | Фреймворк |
| **Spring Security** | 6.x | Аутентификация и авторизация (JWT) |
| **Spring Data JPA** | 3.x | Работа с БД |
| **Spring Cache** | 3.x | Кеширование |
| **Spring Cloud Gateway** | 4.x | API Gateway |
| **Spring Cloud Netflix** | 4.x | Service Discovery (Eureka) |
| **Resilience4j** | 2.x | Circuit Breaker, Retry, Rate Limiter |
| **PostgreSQL** | 17 | Основная база данных |
| **Redis** | 7.4+ | Кеш + сессии + rate limiting |
| **Apache Kafka** | 3.8+ | Асинхронные события |
| **Flyway** | 10.x | Миграции БД |
| **Maven** | 3.9+ | Сборка проекта |
| **Lombok** | 1.18.x | Уменьшение бойлерплейта |
| **MapStruct** | 1.6.x | Маппинг DTO-Entity |

### Инфраструктура

| Технология | Версия | Назначение |
|------------|--------|------------|
| **Docker** | latest | Контейнеризация |
| **Docker Compose** | latest | Оркестрация сервисов |
| **Testcontainers** | 1.20+ | Интеграционные тесты |
| **JUnit 5** | 5.10+ | Unit-тестирование |
| **Mockito** | 5.x | Мокирование |
| **GitHub Actions** | latest | CI/CD |

### Мониторинг и Observability

| Технология | Версия | Назначение |
|------------|--------|------------|
| **Spring Actuator** | 3.x | /health, /metrics |
| **Micrometer** | 1.13+ | Метрики |
| **Prometheus** | 2.54+ | Сбор метрик |
| **Grafana** | 11.x | Визуализация |
| **OpenTelemetry** | 1.40+ | Distributed tracing |
| **Zipkin** | 3.x | Визуализация трейсов |

### Frontend

| Технология | Версия | Назначение |
|------------|--------|------------|
| **React** | 19 | UI библиотека |
| **TypeScript** | 5.x | Типизация |
| **Vite** | 6.x | Сборка |
| **Tailwind CSS** | 4.x | Стили |

---

## Архитектура системы

# webShopProject

Интернет-магазин одежды.

---

## О проекте

Полноценный сайт с:
- Каталогом товаров (одежда: категории, размеры, цвета, остатки)
- Корзиной
- Оформлением заказов
- Симуляцией оплаты
- Email-уведомлениями
- Админ-панелью

---

## Технологический стек (2026)

### Backend

| Технология | Версия | Назначение |
|------------|--------|------------|
| **Java** | 21 (LTS) | Основной язык, Virtual Threads |
| **Spring Boot** | 3.4.x | Фреймворк |
| **Spring Security** | 6.x | Аутентификация и авторизация (JWT) |
| **Spring Data JPA** | 3.x | Работа с БД |
| **Spring Cache** | 3.x | Кеширование |
| **Spring Cloud Gateway** | 4.x | API Gateway |
| **Spring Cloud Netflix** | 4.x | Service Discovery (Eureka) |
| **Resilience4j** | 2.x | Circuit Breaker, Retry, Rate Limiter |
| **PostgreSQL** | 17 | Основная база данных (пользователи, товары, заказы, уведомления) |
| **Redis** | 7.4+ | Кеш + корзина + rate limiting |
| **Apache Kafka** | 3.8+ | Асинхронные события |
| **Flyway** | 10.x | Миграции БД |
| **Maven** | 3.9+ | Сборка проекта |
| **Lombok** | 1.18.x | Уменьшение бойлерплейта |
| **MapStruct** | 1.6.x | Маппинг DTO-Entity |

### Инфраструктура

| Технология | Версия | Назначение |
|------------|--------|------------|
| **Docker** | latest | Контейнеризация |
| **Docker Compose** | latest | Оркестрация сервисов |
| **Testcontainers** | 1.20+ | Интеграционные тесты |
| **JUnit 5** | 5.10+ | Unit-тестирование |
| **Mockito** | 5.x | Мокирование |
| **GitHub Actions** | latest | CI/CD |

### Мониторинг и Observability

| Технология | Версия | Назначение |
|------------|--------|------------|
| **Spring Actuator** | 3.x | /health, /metrics |
| **Micrometer** | 1.13+ | Метрики |
| **Prometheus** | 2.54+ | Сбор метрик |
| **Grafana** | 11.x | Визуализация |
| **OpenTelemetry** | 1.40+ | Distributed tracing |
| **Zipkin** | 3.x | Визуализация трейсов |

### Frontend

| Технология | Версия | Назначение |
|------------|--------|------------|
| **React** | 19 | UI библиотека |
| **TypeScript** | 5.x | Типизация |
| **Vite** | 6.x | Сборка |
| **Tailwind CSS** | 4.x | Стили |

---

## Архитектура системы

```mermaid
flowchart TB
    %% Клиент
    Client[("Пользователь (Браузер)")]
    
    %% Фронтенд
    Frontend[React Frontend\n:3000]
    
    %% Шлюз
    Gateway[API Gateway\nSpring Cloud Gateway\n:8080]
    
    %% Service Discovery
    Eureka[(Eureka\nService Discovery)]
    
    %% Сервисы
    Auth[Auth Service\n:8081\nJWT, регистрация]
    Product[Product Service\n:8082\nКаталог, товары]
    Order[Order Service\n:8083\nЗаказы, корзина]
    Payment[Payment Service\n:8084\nОплата]
    Notification[Notification Service\n:8085\nEmail уведомления]
    
    %% Базы данных и инфраструктура
    PostgreSQL[(PostgreSQL\nПользователи, товары,\nзаказы, уведомления)]
    Redis[(Redis\nКеш + Корзина)]
    Kafka[("Kafka\nБрокер событий")]
    SMTP[(SMTP\nЯндекс/Gmail)]
    
    %% Связи клиент -> фронт -> шлюз
    Client -->|HTTP| Frontend
    Frontend -->|REST API| Gateway
    
    %% Связи шлюз -> сервисы
    Gateway -->|/api/auth/**| Auth
    Gateway -->|/api/products/**| Product
    Gateway -->|/api/orders/**| Order
    Gateway -->|/api/payments/**| Payment
    Gateway -->|/api/notifications/**| Notification
    
    %% Связи сервисы -> БД
    Auth --> PostgreSQL
    Product --> PostgreSQL
    Order --> PostgreSQL
    Notification --> PostgreSQL
    
    %% Кеш
    Product --> Redis
    Order --> Redis
    
    %% Асинхронные события (Kafka)
    Order -->|"OrderCreated"| Kafka
    Kafka -->|"PaymentProcess"| Payment
    Payment -->|"PaymentStatus"| Kafka
    Kafka -->|"SendNotification"| Notification
    
    %% Уведомления (email)
    Notification -->|отправка письма| SMTP
    
    %% Service Discovery (регистрация)
    Auth -.->|регистрация| Eureka
    Product -.->|регистрация| Eureka
    Order -.->|регистрация| Eureka
    Payment -.->|регистрация| Eureka
    Notification -.->|регистрация| Eureka
    Gateway -.->|поиск сервисов| Eureka
    
    %% Стилизация
    style Client fill:#f9f,stroke:#333,stroke-width:2px
    style Frontend fill:#ff9,stroke:#333,stroke-width:2px
    style Gateway fill:#bbf,stroke:#333,stroke-width:2px
    style Eureka fill:#9f9,stroke:#333,stroke-width:2px
    style Kafka fill:#f96,stroke:#333,stroke-width:2px
    style PostgreSQL fill:#9cf,stroke:#333,stroke-width:2px
    style Redis fill:#f6c,stroke:#333,stroke-width:2px
    style SMTP fill:#cf9,stroke:#333,stroke-width:2px