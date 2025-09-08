![Java](https://cdn.icon-icons.com/icons2/2699/PNG/512/java_logo_icon_168609.png)

# Structured Communication (Belgium) – Java 21 + Spring Boot

![Apache 2.0 License](https://img.shields.io/badge/License-Apache2.0-orange)
![Java](https://img.shields.io/badge/Built_with-Java21-blue)
![Junit5](https://img.shields.io/badge/Tested_with-Junit5-teal)
![AssertJ](https://img.shields.io/badge/Asserts_by-AssertJ-purple)
![Spring](https://img.shields.io/badge/Structured_by-SpringBoot-lemon)
![Maven](https://img.shields.io/badge/Powered_by-Maven-pink)
![Swagger](https://img.shields.io/badge/Docs_by-Swagger-yellow)
![OpenAPI](https://img.shields.io/badge/Specs_by-OpenAPI-green)
[![CI](https://github.com/wallaceespindola/structured-comm-full/actions/workflows/ci.yml/badge.svg)](https://github.com/wallaceespindola/structured-comm-full/actions/workflows/ci.yml)

## Introduction

Structure Communication in Belgium, with Java 21 + JUnit 5 + AssertJ + Maven.

A tiny REST API to **generate** and **validate** Belgian structured communications (e.g., `+++123/4567/89095+++`) using
modulo 97 as check-rule.

## Features

- Generate random valid references
- Validate **structured** (`+++XXX/XXXX/XXXXX+++`) and **numeric-only** (12 digits)
- **OpenAPI/Swagger** at `/swagger-ui`
- **Actuator** health at `/actuator/health`
- **Docker** image with `HEALTHCHECK`
- **Makefile** for common tasks
- **GitHub Actions** CI (build, test, push image to GHCR)
- **Static Frontend** at `/` (index.html)
- Identify a VCS within a free-form line (structured and numeric)
- DevTools hot reload for a smooth dev experience
- Health endpoint includes timestamp details

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
- Actuator Info: `http://localhost:8080/actuator/info`

Postman:

- Import the collection at `postman/structured-comm.postman_collection.json`
- Has examples and basic tests for all endpoints

## Endpoints

- `GET /api/comm/generate`
- `POST /api/comm/validate/structured` (body: `{ "value": "+++123/4567/89095+++" }`)
- `GET /api/comm/validate/structured?value=+++123/4567/89095+++`
- `POST /api/comm/validate/numeric` (body: `{ "value": "123456789095" }`)
- `GET /api/comm/validate/numeric/{value}`

### Identify in Line

- `POST /api/comm/identify/structured` (body: `{ "value": "Please pay +++123/4567/89095+++ today" }`)
- `GET /api/comm/identify/structured?value=Please%20pay%20%2B%2B%2B123%2F4567%2F89095%2B%2B%2B%20today`
- `POST /api/comm/identify/numeric` (body: `{ "value": "Ref 123456789095 attached" }`)
- `GET /api/comm/identify/numeric?value=Ref%20123456789095%20attached`

### Notes on GET vs POST encoding

- For GET query parameters, URL encoding rules treat `+` as space. The frontend and Swagger encode values automatically (e.g., `+` → `%2B`, `/` → `%2F`).
- The backend includes normalization to tolerate common client mistakes (trims quotes, retries with spaces as `+`, and reads the raw query string to preserve `+`).
- Nevertheless, the most reliable approach is to use POST with JSON bodies for values that contain special characters.
- The structured format is strict: `+++XXX/XXXX/XXXXX+++`. Inputs like `+++123//4567//89095+++` are intentionally invalid.

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

## Author

- Wallace Espindola, Sr. Software Engineer / Solution Architect / Java & Python Dev
- **LinkedIn:** [linkedin.com/in/wallaceespindola/](https://www.linkedin.com/in/wallaceespindola/)
- **GitHub:** [github.com/wallaceespindola](https://github.com/wallaceespindola)
- **E-mail:** [wallace.espindola@gmail.com](mailto:wallace.espindola@gmail.com)
- **Twitter:** [@wsespindola](https://twitter.com/wsespindola)
- **Gravatar:** [gravatar.com/wallacese](https://gravatar.com/wallacese)
- **Dev Community:** [dev.to/wallaceespindola](https://dev.to/wallaceespindola)
- **DZone Articles:** [DZone Profile](https://dzone.com/users/1254611/wallacese.html)
- **Pulse Articles:** [LinkedIn Articles](https://www.linkedin.com/in/wallaceespindola/recent-activity/articles/)
- **Website:** [W-Tech IT Solutions](https://www.wtechitsolutions.com/)
- **Presentation Slides:** [Speakerdeck](https://speakerdeck.com/wallacese)

## License

- This project is released under the Apache 2.0 License.
- See the [LICENSE](LICENSE) file for details.
- Copyright © 2025 [Wallace Espindola](https://github.com/wallaceespindola/).