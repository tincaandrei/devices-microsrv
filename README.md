#  Energy Management System (EMS)

Distributed microservice platform for managing users and IoT energy devices (e.g., smart AC units).  
Includes authentication, user profiles, device ownership, and a React frontend dashboard.

---

##  Features

###  Authentication & Authorization
- Register/login with username + password  
- JWT-based authentication with role support:
  - `ADMIN`
  - `CLIENT`
- Traefik validates JWTs and injects:
  - `X-User-Id`
  - `X-Role`
  - `X-Username`

###  User Profiles
- One profile per credential UUID  
- Fields:
  - `name`, `email`, `phone`
  - `address`, `city`, `country`
- Profile auto-created on registration  
- Users can view/edit their own profile

###  Device Management
- **Admins**:
  - Create / update / delete devices  
- **Users**:
  - View devices assigned to them  
  - View unassigned devices  
  - Assign available devices to themselves  
  - Unassign their devices  
- Device fields:
  - `name`, `description`
  - `maxConsumption`, `powerConsumption`
  - `ownerId` (nullable)

###  Frontend (React)
Single Page Application with:
- Login / Registration
- Dashboard showing:
  - My Devices
  - Available Devices
  - My Profile (view + edit)

---

##  Architecture Overview

###  Traefik Reverse Proxy (API Gateway)
Routes:
- `/auth` → auth-service  
- `/users` → user-service  
- `/devices` → device-service  

Responsibilities:
- CORS middleware  
- JWT validation via `ForwardAuth` → `/auth/validate`  
- Injects `X-User-Id`, `X-Role`, `X-Username` into downstream services  

---

##  Microservices

###  Auth Service (`auth-service`)
- Spring Boot (Java 17), PostgreSQL  
- Manages credentials:  
  - username, bcrypt password, email, role  
- Issues & validates JWT tokens  
- On registration → creates matching user profile in user-service  

**Endpoints (`/auth`):**
- `POST /register`  
- `POST /login`  
- `GET /me`  
- `GET /validate` (used by Traefik)

---

###  User Service (`user-service`)
- Manages user profiles  
- Aggregates user devices by calling device-service  

**Admin Endpoints:**
- `GET /users`  
- `POST /users`  
- `PUT /users/{id}`  
- `DELETE /users/{id}`  

**For logged-in user:**
- `GET /users/me`  
- `PUT /users/me`  
- `GET /users/me/devices`  

---

###  Device Service (`device-service`)
- Manages devices table with optional `ownerId`  
- Enforces ownership & role-based access via Traefik headers  

**Admin CRUD:**
- `GET /devices`  
- `POST /devices`  
- `PUT /devices/{id}`  
- `DELETE /devices/{id}`  

**User endpoints:**
- `GET /devices/me`  
- `GET /devices/available`  
- `POST /devices/{id}/assign/{userId}`  
- `POST /devices/{id}/unassign`  

**Advanced:**
- `GET /devices/owner/{ownerId}`  

---

##  Repository Structure
- auth-service/ # Authentication microservice
- user-service/ # User profile microservice
- device-service/ # Device management microservice
- frontend/ # React frontend SPA
- docs/ # Diagrams & documentation
- docker-compose.yml # Full infrastructure definition

