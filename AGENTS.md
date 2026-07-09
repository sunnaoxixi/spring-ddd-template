# Repository Guidelines

## Project Structure & Module Organization
This is a Maven Spring Boot DDD template. Main Java code lives under
`src/main/java/com/sunnao/spring/ddd/template/` and is organized by DDD layers:
`adaptor`, `application`, `client`, `domain`, `infrastructure`, `model`, and
`common`. Keep new business features inside the same layer pattern, for example
`domain/system/order` plus matching `application`, `client`, `adaptor`, and
`infrastructure` packages.

Configuration and assets are in `src/main/resources/`. Flyway migrations live in
`src/main/resources/db/migration/` and use names such as `V7__init_order.sql`.
Tests mirror production packages under `src/test/java/`; test configuration is in
`src/test/resources/application-test.yaml`. Do not edit generated `target/`
content.

## Build, Test, and Development Commands
- `.\mvnw.cmd spring-boot:run`: run the app locally with the default dev profile.
- `.\mvnw.cmd test`: run unit tests and eligible integration tests.
- `.\mvnw.cmd clean package`: compile, test, and build the application jar.
- `docker compose up -d`: start local PostgreSQL and Redis dependencies.

On Unix-like shells, use `./mvnw` instead of `.\mvnw.cmd`.

## Coding Style & Naming Conventions
Use Java 25, Spring Boot 4, Lombok, MapStruct, MyBatis-Flex, and Sa-Token
patterns already present in the codebase. Java indentation is four spaces.
Packages are lowercase; classes use clear layer suffixes such as `Controller`,
`AppServiceImpl`, `Aggregate`, `DomainServiceImpl`, `RepositoryImpl`, `Mapper`,
`PO`, `Req`, and `Res`. Keep domain rules in aggregates/domain services, request
validation in request DTO `check()` methods, and persistence details in
`infrastructure`.

## Testing Guidelines
Use JUnit 5 for tests. Mockito-style domain service tests should avoid Spring and
external services where possible. Integration tests belong under
`src/test/java/.../integration` and may require `TEST_PG_URL` and
`TEST_REDIS_HOST`; they are designed to skip when required environment variables
are absent. Name tests `*Test.java` and prefer descriptive method names, e.g.
`createShouldRejectBlankEmail`.

## Commit & Pull Request Guidelines
Recent history uses Conventional Commit-style messages, often scoped:
`feat(auth): ...`, `fix(config): ...`, `refactor: ...`, and `docs: ...`. Keep
commits focused and explain user-visible behavior or migration impact.

Pull requests should include a concise summary, test results such as
`.\mvnw.cmd test`, linked issues when applicable, and notes for database,
configuration, or Docker changes. Include screenshots only for documentation or
UI-facing changes.

## Security & Configuration Tips
Copy `.env.example` to `.env` for local work, but do not commit real secrets.
Use environment variables for production database, Redis, and S3 credentials.
When adding migrations, make them forward-only and safe to run on existing data.
