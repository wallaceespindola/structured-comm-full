![Java](https://cdn.icon-icons.com/icons2/2699/PNG/512/java_logo_icon_168609.png)

# Structured Communication (Belgium) – Java 21 + Spring Boot

![Apache 2.0 License](https://img.shields.io/badge/License-Apache2.0-orange)
![Java](https://img.shields.io/badge/Built_with-Java21-blue)
![Junit5](https://img.shields.io/badge/Tested_with-Junit5-teal)
![Spring](https://img.shields.io/badge/Structured_by-SpringBoot-lemon)
![Maven](https://img.shields.io/badge/Powered_by-Maven-pink)
![Swagger](https://img.shields.io/badge/Docs_by-Swagger-yellow)
![OpenAPI](https://img.shields.io/badge/Specs_by-OpenAPI-purple)
[![CI](https://github.com/wallaceespindola/structured-comm-full/actions/workflows/ci.yml/badge.svg)](https://github.com/wallaceespindola/structured-comm-full/actions/workflows/ci.yml)

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Endpoints](#endpoints)
- [Usage Examples](#usage-examples)
- [Check digits rule](#check-digits-rule)
- [Running Tests](#running-tests)
- [Docker](#docker)
- [Makefile](#makefile)
- [CI – GitHub Actions](#ci--github-actions)
- [Project Structure](#project-structure)
- [Author](#author)
- [License](#license)

## Introduction

A small REST API to **generate** and **validate** Belgian banking structured communications
(*gestructureerde mededeling* / *communication structurée*, e.g. `+++123/4567/89002+++`) using a modulo 97 check rule.
It can also find a reference inside a free-form line of text. Built with Java 21, Spring Boot 3.5, JUnit 5, AssertJ and Maven.

## Features

- Generate random valid references
- Validate **structured** (`+++XXX/XXXX/XXXXX+++`) and **numeric-only** (12 digits)
- Identify a VCS within a free-form line (structured and numeric)
- Every response carries `structured`, `numeric`, `valid`, `reason` and an ISO-8601 `timestamp`
- **OpenAPI/Swagger** at `/swagger-ui`
- **Actuator** health at `/actuator/health` (includes a custom `app` component with timestamp details) and build info at `/actuator/info`
- **Static Frontend** at `/` (index.html) to try every endpoint from the browser
- **Postman** collection with examples and basic tests for all endpoints
- **Docker** image with `HEALTHCHECK`
- **Makefile** for common tasks
- **GitHub Actions** CI (build, test, push image to GHCR)
- DevTools hot reload and LiveReload for local development

## Tech Stack

| Area          | Technology                                                    |
|---------------|---------------------------------------------------------------|
| Language      | Java 21                                                       |
| Framework     | Spring Boot 3.5.16 (Web, Validation, Actuator, DevTools)      |
| API docs      | springdoc-openapi (Swagger UI + OpenAPI 3)                    |
| DTOs          | Java records                                                  |
| Testing       | JUnit 5, AssertJ (`spring-boot-starter-test`)                 |
| Build         | Maven                                                         |
| Container     | Docker (Maven build stage, Eclipse Temurin JRE runtime), Docker Compose |
| CI            | GitHub Actions, GitHub Container Registry (GHCR)              |

## Prerequisites

- JDK 21+
- Maven
- Docker (optional, for the container workflow)

## Quick Start

```bash
git clone https://github.com/wallaceespindola/structured-comm-full.git
cd structured-comm-full
mvn spring-boot:run   # or: make run

# or build the executable jar
mvn clean package
java -jar target/structured-comm-0.0.1.jar
```

The app listens on port `8080`. Open:

- Frontend demo: `http://localhost:8080/`
- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`
- Actuator Info: `http://localhost:8080/actuator/info`

Postman:

- Import the collection at [`postman/structured-comm.postman_collection.json`](postman/structured-comm.postman_collection.json)
- Has examples and basic tests for all endpoints; `{{baseUrl}}` defaults to `http://localhost:8080`

## Endpoints

All endpoints are under `/api/comm` and return JSON.

| Method | Path                               | Input                                   |
|--------|------------------------------------|-----------------------------------------|
| GET    | `/api/comm/generate`               | –                                       |
| POST   | `/api/comm/validate/structured`    | body `{ "value": "+++123/4567/89002+++" }` |
| GET    | `/api/comm/validate/structured`    | query `?value=+++123/4567/89002+++` (URL-encoded) |
| POST   | `/api/comm/validate/numeric`       | body `{ "value": "123456789002" }`      |
| GET    | `/api/comm/validate/numeric/{value}` | path variable                         |

### Identify in Line

| Method | Path                               | Input                                   |
|--------|------------------------------------|-----------------------------------------|
| POST   | `/api/comm/identify/structured`    | body `{ "value": "Please pay +++123/4567/89002+++ today" }` |
| GET    | `/api/comm/identify/structured`    | `?value=Please%20pay%20%2B%2B%2B123%2F4567%2F89002%2B%2B%2B%20today` |
| POST   | `/api/comm/identify/numeric`       | body `{ "value": "Ref 123456789002 attached" }` |
| GET    | `/api/comm/identify/numeric`       | `?value=Ref%20123456789002%20attached`  |

A blank `value` in a POST body is rejected with HTTP 400 (`@NotBlank`). Everything else returns HTTP 200, with
`valid: false` and a `reason` when the input is wrong.

### Notes on GET vs POST encoding

- For GET query parameters, URL encoding rules treat `+` as space. The frontend and Swagger encode values automatically (e.g., `+` → `%2B`, `/` → `%2F`).
- The backend includes normalization to tolerate common client mistakes (trims quotes, retries with spaces as `+`, and reads the raw query string to preserve `+`).
- Nevertheless, the most reliable approach is to use POST with JSON bodies for values that contain special characters.
- The structured format is strict: `+++XXX/XXXX/XXXXX+++`. Inputs like `+++123//4567//89002+++` are intentionally invalid.

## Usage Examples

Generate a random valid reference:

```bash
curl -s http://localhost:8080/api/comm/generate
```

```json
{"structured":"+++494/7159/12444+++","numeric":"494715912444","valid":true,"reason":null,"timestamp":"2026-09-26T17:58:17.706141Z"}
```

Validate a structured value:

```bash
curl -s -X POST http://localhost:8080/api/comm/validate/structured \
  -H 'Content-Type: application/json' \
  -d '{"value":"+++123/4567/89002+++"}'
```

```json
{"structured":"+++123/4567/89002+++","numeric":"123456789002","valid":true,"reason":null,"timestamp":"2026-09-26T17:58:17.753807Z"}
```

Validate a numeric value with wrong check digits:

```bash
curl -s http://localhost:8080/api/comm/validate/numeric/123456789000
```

```json
{"structured":"+++123/4567/89000+++","numeric":"123456789000","valid":false,"reason":"Invalid check digits: expected 02 for base 1234567890","timestamp":"2026-09-26T17:58:17.766347Z"}
```

Find a reference inside a line of text:

```bash
curl -s -X POST http://localhost:8080/api/comm/identify/numeric \
  -H 'Content-Type: application/json' \
  -d '{"value":"Ref 123456789002 attached"}'
```

Other `reason` values you may see: `Format must be +++XXX/XXXX/XXXXX+++`, `Numeric value must be exactly 12 digits`,
`No structured VCS found in input line`, `No numeric 12-digit VCS found in input line`, `Input line must not be blank`.

## Check digits rule

The first 10 digits are the base; the last 2 are the check digits:

`check = base % 97`; if the remainder is `0`, use `97`.

Example: base `1234567890` → `1234567890 % 97 = 2` → check `02` (a remainder of 0 becomes `97`) → `+++123/4567/89002+++`.

## Running Tests

```bash
mvn test      # or: make test
```

[`StructuredCommServiceTest`](src/test/java/com/example/structuredcomm/service/StructuredCommServiceTest.java) has 11 unit
tests covering the check-digit edge case (97), a known-valid real reference, valid and invalid references, strict format
enforcement, and in-line identification (including 12-digit boundary checks).
[`StructuredCommApplicationTests`](src/test/java/com/example/structuredcomm/StructuredCommApplicationTests.java) loads the
Spring context, so an incompatible dependency bump fails CI instead of failing at startup.

## Docker

```bash
docker build -t structured-comm:latest .
docker run --rm -p 8080:8080 structured-comm:latest
```

The image is a two-stage build (Maven build, Temurin JRE runtime), exposes `8080`, accepts extra JVM flags through
`JAVA_OPTS`, and has a `HEALTHCHECK` that polls `/actuator/health`.

### Docker Compose

```bash
docker compose up --build
```

Compose maps port `8080` and sets `JAVA_OPTS=-Xms128m -Xmx256m`.

## Makefile

| Target              | Command                                  |
|---------------------|------------------------------------------|
| `make run`          | `mvn spring-boot:run`                    |
| `make test`         | `mvn -q -DskipITs test`                  |
| `make build`        | `mvn -q -DskipTests package`             |
| `make clean`        | `mvn -q clean`                           |
| `make docker-build` | `docker build -t structured-comm:latest .` |
| `make docker-run`   | `docker run --rm -p 8080:8080 structured-comm:latest` |
| `make compose-up`   | `docker compose up --build`              |
| `make compose-down` | `docker compose down`                    |

## CI – GitHub Actions

See [`.github/workflows/ci.yml`](.github/workflows/ci.yml). Every push (any branch) and pull request runs
`mvn -q -DskipITs test package` on JDK 21, then builds and pushes the image to
`ghcr.io/wallaceespindola/structured-comm-full` tagged `sha-<commit>`, the branch name, and `latest` on `main`.
Dependabot keeps dependencies current ([`.github/dependabot.yml`](.github/dependabot.yml)).

## Project Structure

```
structured-comm-full/
├─ pom.xml
├─ Dockerfile
├─ docker-compose.yml
├─ Makefile
├─ README.md
├─ postman/
│  └─ structured-comm.postman_collection.json
├─ src/
│  ├─ main/java/com/example/structuredcomm/
│  │  ├─ StructuredCommApplication.java
│  │  ├─ actuator/AppHealthIndicator.java
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
└─ .github/
   ├─ dependabot.yml
   └─ workflows/ci.yml
```

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
