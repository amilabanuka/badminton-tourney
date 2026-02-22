# Tournament Management API Documentation

This document describes the Tournament Management API endpoints for the Badminton Manager application.

## Overview

The Tournament Management feature allows:
- **ADMIN** users to create tournaments and manage tournament administrators
- **ADMIN** and **TOURNY_ADMIN** users to manage tournament players
- All **authenticated users** to view tournaments

## Data Model

### Tournament Entity
- `id` (Long) - Unique identifier
- `name` (String) - Unique tournament name
- `ownerId` (Long) - Foreign key to users table (must be TOURNY_ADMIN)
- `enabled` (Boolean) - Tournament status
- `createdAt` (Long) - Timestamp of creation
- `updatedAt` (Long) - Timestamp of last update
- `adminIds` (List<Long>) - List of tournament admin user IDs
- `playerIds` (List<Long>) - List of tournament player user IDs

## API Endpoints

### 1. Get Available Tournament Admins
**GET** `/api/tournaments/admins/available`

Get a list of users with TOURNY_ADMIN role for owner selection.

**Access:** ADMIN only

**Response:**
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "users": [
    {
      "id": 1,
      "username": "johndoe",
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe"
    }
  ]
}
```

---

### 2. Create Tournament
**POST** `/api/tournaments`

Create a new tournament with basic information.

**Access:** ADMIN only

**Request Body:**
```json
{
  "name": "Spring Championship 2024",
  "ownerId": 1,
  "enabled": true
}
```

**Response:**
```json
{
  "success": true,
  "message": "Tournament created successfully",
  "tournament": {
    "id": 1,
    "name": "Spring Championship 2024",
    "ownerId": 1,
    "enabled": true,
    "createdAt": 1708632000000,
    "updatedAt": 1708632000000,
    "adminIds": [],
    "playerIds": []
  }
}
```

**Validation:**
- Tournament name must be unique globally
- Owner ID must reference a user with TOURNY_ADMIN role

---

### 3. Add Tournament Admin
**POST** `/api/tournaments/{id}/admins`

Add a user with TOURNY_ADMIN role as a tournament administrator.

**Access:** ADMIN only

**Request Body:**
```json
{
  "userId": 2
}
```

**Response:**
```json
{
  "success": true,
  "message": "Tournament admin added successfully"
}
```

**Validation:**
- User must have TOURNY_ADMIN role
- User cannot already be a tournament admin

---

### 4. Add Tournament Player
**POST** `/api/tournaments/{id}/players`

Add a user with PLAYER role to the tournament.

**Access:** ADMIN or TOURNY_ADMIN

**Request Body:**
```json
{
  "userId": 3
}
```

**Response:**
```json
{
  "success": true,
  "message": "Tournament player added successfully"
}
```

**Validation:**
- User must have PLAYER role
- User cannot already be a tournament player

---

### 5. Remove Tournament Admin
**DELETE** `/api/tournaments/{id}/admins/{userId}`

Remove a tournament administrator.

**Access:** ADMIN only

**Response:**
```json
{
  "success": true,
  "message": "Tournament admin removed successfully"
}
```

---

### 6. Remove Tournament Player
**DELETE** `/api/tournaments/{id}/players/{userId}`

Remove a player from the tournament.

**Access:** ADMIN or TOURNY_ADMIN

**Response:**
```json
{
  "success": true,
  "message": "Tournament player removed successfully"
}
```

---

### 7. Get All Tournaments
**GET** `/api/tournaments`

Retrieve a list of all tournaments.

**Access:** All authenticated users

**Response:**
```json
{
  "success": true,
  "message": "Tournaments retrieved successfully",
  "tournaments": [
    {
      "id": 1,
      "name": "Spring Championship 2024",
      "ownerId": 1,
      "enabled": true,
      "createdAt": 1708632000000,
      "updatedAt": 1708632000000,
      "adminIds": [1, 2],
      "playerIds": [3, 4, 5]
    }
  ]
}
```

---

### 8. Get Tournament by ID
**GET** `/api/tournaments/{id}`

Retrieve details of a specific tournament.

**Access:** All authenticated users

**Response:**
```json
{
  "success": true,
  "message": "Tournament retrieved successfully",
  "tournament": {
    "id": 1,
    "name": "Spring Championship 2024",
    "ownerId": 1,
    "enabled": true,
    "createdAt": 1708632000000,
    "updatedAt": 1708632000000,
    "adminIds": [1, 2],
    "playerIds": [3, 4, 5]
  }
}
```

---

## Database Schema

### tournament table
```sql
CREATE TABLE IF NOT EXISTS tournament (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    owner_id BIGINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES users(id),
    INDEX idx_owner_id (owner_id),
    INDEX idx_name (name)
);
```

### tournament_admins table
```sql
CREATE TABLE IF NOT EXISTS tournament_admins (
    tournament_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (tournament_id, user_id),
    FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### tournament_players table
```sql
CREATE TABLE IF NOT EXISTS tournament_players (
    tournament_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (tournament_id, user_id),
    FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

## Security Configuration

Role-based access control is configured in `SecurityConfig`:

- `/api/tournaments` POST - ADMIN only
- `/api/tournaments/admins/available` GET - ADMIN only
- `/api/tournaments/*/admins/**` - ADMIN only
- `/api/tournaments/*/players/**` - ADMIN or TOURNY_ADMIN
- `/api/tournaments/**` GET - All authenticated users

## Error Responses

All endpoints return error responses in the following format:

```json
{
  "success": false,
  "message": "Error message describing what went wrong"
}
```

Common HTTP status codes:
- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Validation error or business logic violation
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found

## Example Usage Flow

1. **ADMIN** calls `GET /api/tournaments/admins/available` to get list of users with TOURNY_ADMIN role
2. **ADMIN** selects an owner from the list and creates tournament with `POST /api/tournaments`
3. **ADMIN** adds additional tournament administrators with `POST /api/tournaments/{id}/admins`
4. **ADMIN** or **TOURNY_ADMIN** adds players with `POST /api/tournaments/{id}/players`
5. Any **authenticated user** can view tournaments with `GET /api/tournaments` or `GET /api/tournaments/{id}`

