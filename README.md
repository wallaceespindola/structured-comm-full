# Structured Communication (Belgium) – Java 21 + Spring Boot

A tiny REST API to **generate** and **validate** Belgian structured communications (e.g., `+++123/4567/89095+++`). Now with a super simple **frontend page** under `/` to test the endpoints.

## Features
- Generate random valid references
- Validate **structured** (`+++XXX/XXXX/XXXXX+++`) and **numeric-only** (12 digits)
- **OpenAPI/Swagger** at `/swagger-ui`
- **Actuator** health at `/actuator/health`
- **Docker** image with `HEALTHCHECK`
- **Makefile** for common tasks
- **GitHub Actions** CI (build, test, push image to GHCR)
- **Static Frontend** at `/` (index.html)

## Project Structure
```
structured-comm/
├─ pom.xml
├─ Dockerfile
├─ docker-compose.yml
├─ Makefile
├─ README.md
├─ src/
│  ├─ main/java/com/example/structuredcomm/
│  │  ├─ StructuredCommApplication.java
│  │  ├─ controller/StructuredCommController.java
│  │  ├─ service/StructuredCommService.java
│  │  └─ dto/
│  │     ├─ ValidateRequest.java
│  │     └─ ValidationResponse.java
│  ├─ main/resources/
│  │  ├─ application.yml
│  │  └─ static/
│  │     └─ index.html
│  └─ test/java/com/example/structuredcomm/service/
│     └─ StructuredCommServiceTest.java
└─ .github/workflows/ci.yml
```

## How to run

```bash
mvn spring-boot:run
# or
mvn clean package && java -jar target/structured-comm-0.0.1.jar
```

Open:
- Frontend demo: `http://localhost:8080/`
- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

## Endpoints

- `GET /api/comm/generate`
- `POST /api/comm/validate/structured` (body: `{ "value": "+++123/4567/89095+++" }`)
- `GET /api/comm/validate/structured?value=+++123/4567/89095+++`
- `POST /api/comm/validate/numeric` (body: `{ "value": "123456789095" }`)
- `GET /api/comm/validate/numeric?value=123456789095`

## Docker

```bash
docker build -t structured-comm:latest .
docker run --rm -p 8080:8080 structured-comm:latest
```

### Docker Compose
```bash
docker compose up --build
```

## Makefile
```bash
make run
make test
make build
make docker-build
make docker-run
make compose-up
make compose-down
```

## CI – GitHub Actions
See `.github/workflows/ci.yml`. Push to GitHub to trigger build, tests, and image push to GHCR.

## Check digits rule
`check = 97 - (base % 97)`; if result is `0`, use `97`.
