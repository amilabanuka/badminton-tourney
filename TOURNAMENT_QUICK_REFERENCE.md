# Tournament Management Quick Reference

## Quick Start

### 1. Authentication
All tournament endpoints require HTTP Basic Authentication with valid user credentials.

### 2. Role Requirements
- **ADMIN**: Can do everything
- **TOURNY_ADMIN**: Can manage players, view tournaments  
- **PLAYER**: Can view tournaments

---

## API Endpoints Quick Reference

### Get Available Owners
```http
GET /api/tournaments/admins/available
Auth: ADMIN
```
Returns list of users with TOURNY_ADMIN role to select as tournament owner.

---

### Create Tournament
```http
POST /api/tournaments
Auth: ADMIN
Content-Type: application/json

{
  "name": "Tournament Name",
  "ownerId": 1,
  "enabled": true
}
```

---

### Add Tournament Admin
```http
POST /api/tournaments/{tournamentId}/admins
Auth: ADMIN
Content-Type: application/json

{
  "userId": 2
}
```

---

### Add Tournament Player
```http
POST /api/tournaments/{tournamentId}/players
Auth: ADMIN or TOURNY_ADMIN
Content-Type: application/json

{
  "userId": 3
}
```

---

### Remove Tournament Admin
```http
DELETE /api/tournaments/{tournamentId}/admins/{userId}
Auth: ADMIN
```

---

### Remove Tournament Player
```http
DELETE /api/tournaments/{tournamentId}/players/{userId}
Auth: ADMIN or TOURNY_ADMIN
```

---

### List All Tournaments
```http
GET /api/tournaments
Auth: Any authenticated user
```

---

### Get Tournament Details
```http
GET /api/tournaments/{tournamentId}
Auth: Any authenticated user
```

---

## Common Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "tournament": { ... }  // or "tournaments": [ ... ]
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description"
}
```

---

## Validation Rules

1. **Tournament Name**: Must be unique globally
2. **Owner**: Must be a user with TOURNY_ADMIN role
3. **Admin**: Must be a user with TOURNY_ADMIN role
4. **Player**: Must be a user with PLAYER role
5. **Duplicates**: Cannot add same user twice as admin or player

---

## Database Tables

### tournament
- id (PK)
- name (UNIQUE)
- owner_id (FK → users)
- enabled
- created_at
- updated_at

### tournament_admins
- tournament_id (FK → tournament)
- user_id (FK → users)
- PRIMARY KEY (tournament_id, user_id)

### tournament_players
- tournament_id (FK → tournament)
- user_id (FK → users)
- PRIMARY KEY (tournament_id, user_id)

---

## Typical Workflow

1. **ADMIN** retrieves list of possible owners:
   ```
   GET /api/tournaments/admins/available
   ```

2. **ADMIN** creates tournament:
   ```
   POST /api/tournaments
   {
     "name": "Spring Championship",
     "ownerId": 1,
     "enabled": true
   }
   ```

3. **ADMIN** adds tournament administrators:
   ```
   POST /api/tournaments/1/admins
   { "userId": 2 }
   ```

4. **ADMIN** or **TOURNY_ADMIN** adds players:
   ```
   POST /api/tournaments/1/players
   { "userId": 3 }
   ```

5. Any **authenticated user** can view:
   ```
   GET /api/tournaments
   GET /api/tournaments/1
   ```

---

## HTTP Status Codes

- `200 OK` - Success (GET, DELETE)
- `201 Created` - Tournament created successfully
- `400 Bad Request` - Validation error
- `401 Unauthorized` - Not authenticated
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found

---

## Testing

Run unit tests:
```bash
cd backend
mvn test -Dtest=TournamentServiceTest
```

All 14 tests should pass ✅

---

## Files Created/Modified

### New Files
- Tournament.java
- TournamentRepository.java
- TournamentService.java
- TournamentController.java
- CreateTournamentRequest.java
- TournamentResponse.java
- AddTournamentAdminRequest.java
- AddTournamentPlayerRequest.java
- UserListResponse.java
- TournamentServiceTest.java

### Modified Files
- schema.sql (added 3 tables)
- UserRepository.java (added findByRole)
- SecurityConfig.java (added role-based access)

---

## Further Reading

- [TOURNAMENT_API_DOCUMENTATION.md](TOURNAMENT_API_DOCUMENTATION.md) - Detailed API reference
- [TOURNAMENT_IMPLEMENTATION_SUMMARY.md](TOURNAMENT_IMPLEMENTATION_SUMMARY.md) - Implementation details
- [plan-tournamentManagement.prompt.md](plan-tournamentManagement.prompt.md) - Original plan

