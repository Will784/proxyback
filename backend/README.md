# LogHub Backend

Microservice-style log management API built with **Spring Boot 3 / Java 17** and the **Proxy pattern**.
Each log domain is a specialised service hidden behind its own proxy.

## Pattern map

| Pattern role   | Class                                                                              |
|----------------|------------------------------------------------------------------------------------|
| Subject        | `LogService` (interface)                                                           |
| RealSubject    | `SecurityLogService`, `IngestionLogService`, `TraceLogService`, `AnalyticsLogService` (extend `AbstractLogService`) |
| Proxy (base)   | `AbstractLogProxy` (same interface + reference to the real service, Template Method) |
| Concrete proxy | `SecurityLogProxy`, `IngestionLogProxy`, `TraceLogProxy`, `AnalyticsLogProxy`       |
| Client         | `LogController` (knows only `LogService`, receives proxies via `LogServiceRegistry`) |

| Domain    | Proxy kind               | What it does |
|-----------|--------------------------|--------------|
| SECURITY  | Protection proxy         | RBAC by role, masks IP/payload for analysts, audits every decision |
| INGESTION | Validation / rate limit  | Business validation, token bucket per client, duplicate suppression |
| TRACE     | Virtual + caching proxy  | Summaries in lists, lazy payload load, LRU cache with TTL |
| ANALYTICS | Smart proxy              | TTL cache for aggregates, invalidated on every write |

Algorithms: token bucket (O(1)), LRU with TTL via access-ordered `LinkedHashMap` (O(1)), bounded ring buffer for audit,
SHA-256 fingerprints, constructor projections so heavy payloads are never fetched in lists.

## Run locally

```bash
mvn spring-boot:run            # H2 in memory, demo data seeded, port 8080
mvn test
```

## API (identity via headers `X-Role`: ADMIN | ANALYST | SERVICE | GUEST, `X-Client-Id`)

| Method | Path                              | Description |
|--------|-----------------------------------|-------------|
| POST   | `/api/v1/logs/{domain}`           | Save a log |
| GET    | `/api/v1/logs/{domain}?limit=20`  | Recent summaries |
| GET    | `/api/v1/logs/{domain}/{id}`      | Full log with payload |
| GET    | `/api/v1/logs/{domain}/stats`     | Counts by level |
| GET    | `/api/v1/proxies`                 | Proxy overview + metrics |
| GET    | `/api/v1/proxies/audit?limit=30`  | Latest proxy decisions |

`{domain}` = `security`, `ingestion`, `trace`, `analytics`.

```bash
curl -X POST localhost:8080/api/v1/logs/security \
  -H "Content-Type: application/json" -H "X-Role: SERVICE" -H "X-Client-Id: demo" \
  -d '{"source":"auth-gateway","level":"INFO","message":"Login ok"}'
```

## Environment variables

| Variable       | Default                       | Purpose |
|----------------|-------------------------------|---------|
| `DB_URL`       | in-memory H2                  | e.g. `jdbc:postgresql://host:5432/loghub` |
| `DB_USER` / `DB_PASSWORD` | `sa` / empty       | Database credentials |
| `CORS_ORIGINS` | `http://localhost:5500,...`   | Comma-separated frontend origins |
| `SEED_ENABLED` | `true`                        | Load demo data when the table is empty |
| `PORT`         | `8080`                        | HTTP port |

## Deploy (Render + free PostgreSQL)

1. Push this repo to GitHub: `https://github.com/YOUR_USER/loghub-backend`.
2. Render -> New -> PostgreSQL (free). Copy host, database, user, password.
3. Render -> New -> Web Service -> connect the repo (it detects the `Dockerfile`).
4. Set `DB_URL`, `DB_USER`, `DB_PASSWORD`, `CORS_ORIGINS=https://YOUR_USER.github.io`.
5. Backend URL: `https://loghub-backend.onrender.com` (use it in the frontend `AppConfig.js`).
