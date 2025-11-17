# Energy Management System

Distributed microservice system that allows administrators and clients to authenticate, manage profiles, and manage IoT energy devices. The solution mirrors the reference project structure (entities, DTOs, services, controllers, repositories) while introducing three isolated Spring Boot services wired together with Traefik and PostgreSQL.

## Architecture
- **Traefik** reverse proxy handles routing via Docker labels. Requests hit `http://localhost` and are dispatched by path prefix:
  - `/auth` → Auth Service
  - `/users` → User Service
  - `/devices` → Device Service
- **Auth Service** owns credential data, password hashing (BCrypt), JWT issuing + validation.
- **User Service** manages profile data (address, phone, etc.) and aggregates assigned devices via REST calls to the Device Service.
- **Device Service** stores device metadata, CRUD, and user assignment.
- **PostgreSQL** runs as three separate containers (one per service) to enforce bounded contexts.
- **Swagger/OpenAPI** powered by `springdoc` for each service (`/swagger-ui.html`).
- **Docker Compose** provides one-step local deployment (`docker-compose.yml`).

See `docs/deployment-diagram.puml` for the UML deployment diagram.

## Repository Layout
```
energy-management-system/
├─ auth-service/        # Spring Boot credential microservice
├─ user-service/        # Spring Boot profile microservice
├─ device-service/      # Spring Boot device microservice
├─ db/                  # Handy schema scripts + Docker volumes
├─ docs/                # Solution description + UML diagram
└─ docker-compose.yml   # Traefik + services + PostgreSQL cluster
```

Each service follows the same layered structure as the provided example (`controllers`, `dtos`, `entities`, `repositories`, `services`, `handlers`, etc.) and uses `application.properties` (not YAML) for configuration.

## Prerequisites
- Docker & Docker Compose
- Java 17+ (only required for local `mvn spring-boot:run`)
- Node/React frontend is not bundled yet; APIs are ready for integration.

## Running with Docker Compose
```bash
cd energy-management-system
# 1. build images (optional because compose will build automatically)
docker compose build
# 2. start everything (Traefik + 3 services + 3 PostgreSQL instances)
docker compose up -d
# 3. inspect logs
docker compose logs -f auth-service
```
Access points once containers are healthy:
- Traefik dashboard: http://localhost:8080
- Auth API: http://localhost/auth
- User API: http://localhost/users
- Device API: http://localhost/devices

To stop:
```bash
docker compose down -v
```

## Environment Variables
All services expose env vars (see `docker-compose.yml`). Common ones:
| Variable | Default | Description |
| --- | --- | --- |
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | service-specific | PostgreSQL connectivity |
| `JWT_SECRET` | `energy-secret-key` | Shared HMAC secret for JWT signing/verification |
| `PORT` | 8081/8082/8083 | Internal HTTP port |
| `DEVICE_SERVICE_URL` (user-service) | `http://device-service:8083` | Internal DNS name used for cross-service calls |

Override them via `.env` or `docker compose --env-file` as needed.

## Quickstart Flow
1. **Register Admin Credential**
   ```bash
   curl -X POST http://localhost/auth/register \
     -H 'Content-Type: application/json' \
     -d '{"username":"admin","password":"Passw0rd!","role":"ADMIN","email":"admin@example.com"}'
   ```
2. **Login to receive JWT**
   ```bash
   TOKEN=$(curl -s -X POST http://localhost/auth/login \
     -H 'Content-Type: application/json' \
     -d '{"username":"admin","password":"Passw0rd!"}' | jq -r .token)
   ```
3. **Create User Profile** (id must match credential id created earlier or any UUID you want to manage)
   ```bash
   curl -X POST http://localhost/users \
     -H "Authorization: Bearer $TOKEN" \
     -H 'Content-Type: application/json' \
     -d '{"id":"<credential-uuid>","firstName":"Jane","lastName":"Doe","email":"jane@example.com","phoneNumber":"0700000000","address":"Main st","city":"Cluj","country":"RO"}'
   ```
4. **Create Device + assign**
   ```bash
   DEVICE_ID=$(curl -s -X POST http://localhost/devices \
     -H "Authorization: Bearer $TOKEN" \
     -H 'Content-Type: application/json' \
     -d '{"name":"Boiler","description":"Smart boiler","maximumConsumption":2500,"powerConsumption":2200}' | jq -r .id)
   curl -X POST http://localhost/devices/$DEVICE_ID/assign/<user-id> \
     -H "Authorization: Bearer $TOKEN"
   ```
5. **Client View** – login as a `CLIENT` user, then call `GET /devices/me` or `GET /users/me/devices` with the client token to see assigned devices only.

## API Highlights
### Auth Service (`/auth`)
| Method | Path | Role | Purpose |
| --- | --- | --- | --- |
| POST | `/register` | public | Create credential (ADMIN/CLIENT) + returns JWT |
| POST | `/login` | public | Authenticate + returns JWT |
| GET | `/me` | any | Returns credential info tied to JWT |
| GET | `/validate` | public | Validates token and returns claims |

### User Service (`/users`)
| Method | Path | Role | Purpose |
| --- | --- | --- | --- |
| GET | `/` | ADMIN | List profiles |
| POST | `/` | ADMIN | Create profile (id = credential id) |
| GET | `/{id}` | ADMIN or owner | Fetch profile |
| PUT | `/{id}` | ADMIN | Update profile |
| DELETE | `/{id}` | ADMIN | Delete profile |
| GET | `/{id}/devices` | ADMIN or owner | Profile + assigned devices |
| GET | `/me` | any | Current profile |
| GET | `/me/devices` | any | Current profile + devices |

### Device Service (`/devices`)
| Method | Path | Role | Purpose |
| --- | --- | --- | --- |
| GET | `/` | ADMIN | List devices |
| POST | `/` | ADMIN | Create device |
| GET | `/{id}` | ADMIN or owner | Fetch device |
| PUT | `/{id}` | ADMIN | Update device |
| DELETE | `/{id}` | ADMIN | Delete device |
| POST | `/{id}/assign/{userId}` | ADMIN | Assign device |
| POST | `/{id}/unassign` | ADMIN | Unassign device |
| GET | `/owner/{ownerId}` | ADMIN or owner | List owner devices |
| GET | `/me` | any | Devices tied to JWT owner |

## Testing & Tooling
- Use the bundled Swagger UIs (`/swagger-ui.html`) for interactive documentation/testing per service.
- `postman_collection.json` can be composed from the above endpoints (not included by default).
- Unit/integration tests can be added with `@DataJpaTest`/`@SpringBootTest`; skeleton remains from Spring Initializr.

## Database Notes
Sample schema definitions live in `db/*.sql`. Actual tables are auto-managed via Hibernate (`spring.jpa.hibernate.ddl-auto=update`). For grading that requires dumps, run `pg_dump` against each container and store outputs under `db/`.

## Next Steps
1. Implement the React/Angular frontend that consumes these APIs (login form, admin/client dashboards).
2. Add message broker or async telemetry ingestion if required by future iterations.
3. Harden Traefik (TLS certs, JWT validation middleware) for production deployments.

For any clarifications, review `docs/SOLUTION.md` or inspect the service-specific code under `auth-service`, `user-service`, and `device-service`.
"# devices-microsrv" 
