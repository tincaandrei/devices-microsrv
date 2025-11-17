# Swagger-style API Documentation

This document summarizes the main REST APIs exposed by the three backend services, in a Swagger-style format: path, HTTP method, short description, input parameters and response.

All endpoints are exposed through Traefik at `http://localhost` with these base paths:

- Auth Service: `/auth`
- User Service: `/users`
- Device Service: `/devices`

Authenticated endpoints expect a `Bearer` JWT in the `Authorization` header.

---

## Auth Service (`/auth`)

### `POST /auth/register`
- **Description**: Register a new credential (ADMIN or CLIENT) and receive a JWT.
- **Input** (JSON body – `RegisterRequest`):
  - `username` (string, required)
  - `password` (string, required, min 8 chars)
  - `email` (string, optional but recommended, valid email)
  - `role` (string, optional: `ADMIN` or `CLIENT`, defaults to `CLIENT`)
- **Response** (`AuthResponse` JSON):
  - `credentialId` (UUID)
  - `username` (string)
  - `role` (`ADMIN` or `CLIENT`)
  - `token` (string, JWT)
  - `expiresAt` (ISO-8601 timestamp)

### `POST /auth/login`
- **Description**: Authenticate an existing user and receive a JWT.
- **Input** (JSON body – `LoginRequest`):
  - `username` (string, required)
  - `password` (string, required)
- **Response** (`AuthResponse` JSON):
  - Same structure as `/auth/register`.

### `GET /auth/me`
- **Description**: Return current credential details based on the authenticated session.
- **Input**:
  - `Authorization: Bearer <token>`
- **Response** (`CredentialDTO` JSON):
  - `id` (UUID)
  - `username` (string)
  - `role` (`ADMIN` or `CLIENT`)
  - `email` (string, nullable)

### `GET /auth/validate`
- **Description**: Validate a JWT (used by Traefik ForwardAuth).
- **Input**:
  - `Authorization: Bearer <token>`
- **Response** (`TokenValidationResponse` JSON):
  - `valid` (boolean)
  - `credentialId` (UUID, nullable)
  - `username` (string, nullable)
  - `role` (`ADMIN` or `CLIENT`, nullable)
  - `expiresAt` (ISO-8601 timestamp, nullable)
- **Headers on successful validation** (used by Traefik):
  - `X-User` (username)
  - `X-User-Id` (UUID as string)
  - `X-Role` (`ROLE_ADMIN` or `ROLE_USER`)

---

## User Service (`/users`)

### `GET /users`
- **Description**: List all user profiles.
- **Auth**: Requires admin (`X-Role = ROLE_ADMIN`).
- **Input**:
  - `Authorization: Bearer <token>`
- **Response**: Array of `UserProfileDTO`:
  - `id` (UUID)
  - `firstName`, `lastName` (string)
  - `email` (string)
  - `phoneNumber` (string, nullable)
  - `address`, `city`, `country` (string, nullable)

### `POST /users`
- **Description**: Create a new user profile (id usually equals credential id).
- **Auth**: Admin only.
- **Input** (JSON body – `UserProfileRequest`):
  - `id` (UUID, required)
  - `firstName` (string, required)
  - `lastName` (string, required)
  - `email` (string, required, unique)
  - `phoneNumber` (string, optional, 6–32 chars)
  - `address`, `city`, `country` (string, optional)
- **Response** (`UserProfileDTO`):
  - Same fields as request, plus any persisted values.

### `GET /users/{id}`
- **Description**: Fetch a specific user profile.
- **Auth**: Admin **or** the profile owner.
- **Input**:
  - Path variable: `id` (UUID)
  - Header: `Authorization: Bearer <token>` (Traefik injects `X-Role`, `X-User-Id`)
- **Response**: `UserProfileDTO`.

### `PUT /users/{id}`
- **Description**: Update a user profile (admin-level operation).
- **Auth**: Admin only.
- **Input**:
  - Path variable: `id` (UUID)
  - Body: `UserProfileRequest` JSON (id must match path id).
- **Response**: Updated `UserProfileDTO`.

### `DELETE /users/{id}`
- **Description**: Delete a user profile.
- **Auth**: Admin only.
- **Input**:
  - Path variable: `id` (UUID)
- **Response**: HTTP 204 No Content on success.

### `GET /users/{id}/devices`
- **Description**: Get profile and assigned devices for a specific user.
- **Auth**: Admin or that user.
- **Input**:
  - Path variable: `id` (UUID)
  - Headers:
    - `Authorization: Bearer <token>`
    - Traefik-injected: `X-User-Id`, `X-Role`
- **Response** (`UserDevicesResponse` JSON):
  - `user` (`UserProfileDTO`)
  - `devices` (array of `DeviceDTO` from user-service perspective):
    - `id` (UUID)
    - `name` (string)
    - `maximumConsumption` (number)
    - `ownerId` (UUID)

### `GET /users/me`
- **Description**: Get current user’s profile.
- **Input**:
  - `Authorization: Bearer <token>` (Traefik injects `X-User-Id`)
- **Response**: `UserProfileDTO` for the authenticated user.

### `GET /users/me/devices`
- **Description**: Get current user’s profile plus their devices.
- **Input**:
  - `Authorization: Bearer <token>`
- **Response** (`UserDevicesResponse`):
  - `user` (`UserProfileDTO`)
  - `devices` (array of `DeviceDTO`).

### `PUT /users/me`
- **Description**: Update current user’s profile fields.
- **Input**:
  - `Authorization: Bearer <token>`
  - Body: `UserProfileRequest` JSON with `id` equal to authenticated user id.
- **Response**:
  - Updated `UserProfileDTO`.

---

## Device Service (`/devices`)

### `GET /devices`
- **Description**: List all devices in the system.
- **Auth**: Admin only.
- **Input**:
  - `Authorization: Bearer <token>` (admin).
- **Response**: Array of `DeviceDTO`:
  - `id` (UUID)
  - `name` (string)
  - `description` (string, nullable)
  - `maximumConsumption` (number)
  - `powerConsumption` (number)
  - `ownerId` (UUID, nullable)

### `POST /devices`
- **Description**: Create a new device (unassigned).
- **Auth**: Admin only.
- **Input** (JSON body – `DeviceRequest`):
  - `name` (string, required)
  - `description` (string, optional)
  - `maximumConsumption` (number, required, ≥ 0.1)
  - `powerConsumption` (number, required, ≥ 0.0)
- **Response** (`DeviceDTO`): Created device (with generated `id`).

### `GET /devices/{id}`
- **Description**: Fetch a device by id.
- **Auth**: Admin or device owner.
- **Input**:
  - Path variable: `id` (UUID)
  - `Authorization: Bearer <token>` (Traefik injects `X-Role`, `X-User-Id`)
- **Response**: `DeviceDTO`.

### `PUT /devices/{id}`
- **Description**: Update an existing device.
- **Auth**: Admin only.
- **Input**:
  - Path variable: `id` (UUID)
  - Body: `DeviceRequest` JSON.
- **Response**: Updated `DeviceDTO`.

### `DELETE /devices/{id}`
- **Description**: Delete a device.
- **Auth**: Admin only.
- **Input**:
  - Path variable: `id` (UUID)
- **Response**: HTTP 204 No Content on success.

### `POST /devices/{id}/assign/{userId}`
- **Description**: Assign a device to a user.
- **Auth**:
  - Admin: can assign to any `userId`.
  - Non-admin: can only assign to themselves (path `userId` must equal `X-User-Id`).
- **Input**:
  - Path variables:
    - `id` (UUID device id)
    - `userId` (UUID user id)
  - Headers: `Authorization: Bearer <token>` (Traefik provides `X-Role`, `X-User-Id`).
- **Response**: Updated `DeviceDTO` with `ownerId` set.

### `POST /devices/{id}/unassign`
- **Description**: Unassign a device.
- **Auth**:
  - Admin: can unassign any device.
  - Non-admin: can only unassign devices they currently own.
- **Input**:
  - Path variable: `id` (UUID)
  - `Authorization: Bearer <token>`
- **Response**: Updated `DeviceDTO` with `ownerId = null`.

### `GET /devices/owner/{ownerId}`
- **Description**: List devices assigned to a specific owner id.
- **Auth**: Admin or that owner.
- **Input**:
  - Path variable: `ownerId` (UUID)
  - `Authorization: Bearer <token>` (Traefik injects headers).
- **Response**: Array of `DeviceDTO`.

### `GET /devices/me`
- **Description**: List devices for the currently authenticated user.
- **Input**:
  - `Authorization: Bearer <token>` (Traefik injects `X-User-Id`).
- **Response**: Array of `DeviceDTO`.

### `GET /devices/available`
- **Description**: List all devices that are not currently assigned to any user.
- **Input**:
  - `Authorization: Bearer <token>` (any authenticated role).
- **Response**: Array of `DeviceDTO` where `ownerId` is null.

---

For full interactive documentation and schemas, start the stack and open Swagger UI for each service:

- Auth Service: `http://localhost/auth/swagger-ui.html`
- User Service: `http://localhost/users/swagger-ui.html`
- Device Service: `http://localhost/devices/swagger-ui.html`

