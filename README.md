# QCVMT Backend

## Tech Stack
- Java 17
- Spring Boot 3.5.16
- Spring Data JPA
- Spring Security OAuth2 Resource Server
- Keycloak OIDC
- MySQL + Oracle (N4 read-only)
- Gradle Kotlin DSL

## Build
```bash
./gradlew build
```

## Run
```bash
./gradlew bootRun
```

## Environment Variables
- MYSQL_HOST
- MYSQL_PORT
- MYSQL_DB
- MYSQL_USER
- MYSQL_PASSWORD
- N4_HOST
- N4_PORT
- N4_SERVICE
- N4_USER
- N4_PASSWORD
- KEYCLOAK_URL
- KEYCLOAK_REALM
- KEYCLOAK_CLIENT_ID
- KEYCLOAK_CLIENT_SECRET
- CORS_ORIGINS

## API Docs
- http://localhost:8080/swagger-ui/index.html

## Health Check
- http://localhost:8080/actuator/health
