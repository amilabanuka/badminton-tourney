# Tournament Management System Architecture

## Complete System Overview

```
┌───────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                                 │
│  HTTP Requests with Basic Auth (username:password)                    │
└───────────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌───────────────────────────────────────────────────────────────────────┐
│                      SECURITY LAYER                                   │
│  SecurityConfig - Role-Based Access Control                           │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │ ADMIN           → Full access to all tournament endpoints       │  │
│  │ TOURNY_ADMIN    → Manage players, view tournaments              │  │
│  │ PLAYER          → View tournaments only                         │  │
│  └─────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌───────────────────────────────────────────────────────────────────────┐
│                      CONTROLLER LAYER                                 │
│  TournamentController - REST API Endpoints                            │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │ GET  /api/tournaments/admins/available  (ADMIN)                 │  │
│  │ POST /api/tournaments                   (ADMIN)                 │  │
│  │ POST /api/tournaments/{id}/admins       (ADMIN)                 │  │
│  │ POST /api/tournaments/{id}/players      (ADMIN|TOURNY_ADMIN)    │  │
│  │ DEL  /api/tournaments/{id}/admins/{uid} (ADMIN)                 │  │
│  │ DEL  /api/tournaments/{id}/players/{uid}(ADMIN|TOURNY_ADMIN)    │  │
│  │ GET  /api/tournaments                   (Authenticated)         │  │
│  │ GET  /api/tournaments/{id}              (Authenticated)         │  │
│  └─────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌───────────────────────────────────────────────────────────────────────┐
│                       SERVICE LAYER                                   │
│  TournamentService - Business Logic & Validation                      │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │ ✓ Validate tournament name uniqueness                          │  │
│  │ ✓ Validate owner has TOURNY_ADMIN role                         │  │
│  │ ✓ Validate admin has TOURNY_ADMIN role                         │  │
│  │ ✓ Validate player has PLAYER role                              │  │
│  │ ✓ Prevent duplicate assignments                                │  │
│  │ ✓ Transactional operations                                     │  │
│  └─────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌───────────────────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                                   │
│  Spring Data JDBC Repositories                                        │
│  ┌──────────────────────────┐  ┌──────────────────────────────────┐  │
│  │  TournamentRepository    │  │  UserRepository                  │  │
│  │  - CRUD operations       │  │  - Find by username/email        │  │
│  │  - Find by name/owner    │  │  - Find by role                  │  │
│  │  - Admin/player mgmt     │  │  - Exists checks                 │  │
│  └──────────────────────────┘  └──────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌───────────────────────────────────────────────────────────────────────┐
│                       DATABASE LAYER                                  │
│  MySQL Database                                                       │
│  ┌─────────────────────────────────────────────────────────────────┐  │
│  │  ┌─────────────┐       ┌──────────────────┐                    │  │
│  │  │   users     │       │   tournament     │                    │  │
│  │  ├─────────────┤       ├──────────────────┤                    │  │
│  │  │ id (PK)     │       │ id (PK)          │                    │  │
│  │  │ username    │◄──────┤ owner_id (FK)    │                    │  │
│  │  │ email       │       │ name (UNIQUE)    │                    │  │
│  │  │ password    │       │ enabled          │                    │  │
│  │  │ first_name  │       │ created_at       │                    │  │
│  │  │ last_name   │       │ updated_at       │                    │  │
│  │  │ role        │       └──────────────────┘                    │  │
│  │  │ enabled     │              │      │                         │  │
│  │  └─────────────┘              │      │                         │  │
│  │         │                     │      │                         │  │
│  │         │     ┌───────────────┘      └─────────────┐           │  │
│  │         │     │                                    │           │  │
│  │         │     ▼                                    ▼           │  │
│  │         │  ┌──────────────────┐       ┌──────────────────┐    │  │
│  │         │  │tournament_admins │       │tournament_players│    │  │
│  │         │  ├──────────────────┤       ├──────────────────┤    │  │
│  │         └─►│ tournament_id(PK)│       │ tournament_id(PK)│    │  │
│  │            │ user_id (PK)     │       │ user_id (PK)     │    │  │
│  │            └──────────────────┘       └──────────────────┘    │  │
│  │                   (FK cascade delete)    (FK cascade delete)  │  │
│  └─────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────┘
```

## Data Flow Example: Create Tournament

```
1. CLIENT
   │
   POST /api/tournaments
   Headers: Authorization: Basic YWRtaW46cGFzc3dvcmQ=
   Body: {"name": "Spring 2024", "ownerId": 1, "enabled": true}
   │
   ▼
2. SECURITY LAYER
   │
   ✓ Decode Basic Auth credentials
   ✓ Authenticate user
   ✓ Check user has ADMIN role
   │
   ▼
3. CONTROLLER
   │
   TournamentController.createTournament()
   │
   ▼
4. SERVICE LAYER
   │
   TournamentService.createTournament()
   ├─► Validate: name is not null/empty
   ├─► Check: tournament name doesn't exist (query DB)
   ├─► Validate: owner exists (query DB)
   ├─► Validate: owner has TOURNY_ADMIN role
   ├─► Create: new Tournament entity
   └─► Save: to database
   │
   ▼
5. REPOSITORY LAYER
   │
   TournamentRepository.save()
   ├─► Spring Data JDBC generates SQL
   └─► Execute INSERT statement
   │
   ▼
6. DATABASE
   │
   INSERT INTO tournament (name, owner_id, enabled, created_at, updated_at)
   VALUES ('Spring 2024', 1, true, 1708632000000, 1708632000000)
   │
   ▼
7. RESPONSE
   │
   200 OK
   {
     "success": true,
     "message": "Tournament created successfully",
     "tournament": {
       "id": 1,
       "name": "Spring 2024",
       "ownerId": 1,
       "enabled": true,
       "createdAt": 1708632000000,
       "updatedAt": 1708632000000,
       "adminIds": [],
       "playerIds": []
     }
   }
```

## Role-Based Access Matrix

| Endpoint | ADMIN | TOURNY_ADMIN | PLAYER | Anonymous |
|----------|-------|--------------|--------|-----------|
| GET /api/tournaments/admins/available | ✅ | ❌ | ❌ | ❌ |
| POST /api/tournaments | ✅ | ❌ | ❌ | ❌ |
| POST /api/tournaments/{id}/admins | ✅ | ❌ | ❌ | ❌ |
| POST /api/tournaments/{id}/players | ✅ | ✅ | ❌ | ❌ |
| DELETE /api/tournaments/{id}/admins/{uid} | ✅ | ❌ | ❌ | ❌ |
| DELETE /api/tournaments/{id}/players/{uid} | ✅ | ✅ | ❌ | ❌ |
| GET /api/tournaments | ✅ | ✅ | ✅ | ❌ |
| GET /api/tournaments/{id} | ✅ | ✅ | ✅ | ❌ |

## Component Interaction Diagram

```
┌──────────────┐
│   Browser    │
│   /Postman   │
└──────┬───────┘
       │ HTTP Request + Basic Auth
       ▼
┌──────────────────────────────────────────┐
│         Spring Security Filter           │
│  - BasicAuthenticationFilter             │
│  - AuthorizationFilter                   │
└──────┬───────────────────────────────────┘
       │ Authenticated Request
       ▼
┌──────────────────────────────────────────┐
│      @RestController                     │
│   TournamentController                   │
│  - Route mapping                         │
│  - HTTP status handling                  │
│  - DTO conversion                        │
└──────┬───────────────────────────────────┘
       │ DTO Request
       ▼
┌──────────────────────────────────────────┐
│         @Service                         │
│    TournamentService                     │
│  - Business logic                        │
│  - Validation rules                      │
│  - @Transactional                        │
└──────┬───────────────────────────────────┘
       │ Entity operations
       ▼
┌──────────────────────────────────────────┐
│      @Repository                         │
│  TournamentRepository / UserRepository   │
│  - Spring Data JDBC                      │
│  - Auto query generation                 │
└──────┬───────────────────────────────────┘
       │ SQL queries
       ▼
┌──────────────────────────────────────────┐
│         MySQL Database                   │
│  - tournament table                      │
│  - tournament_admins table               │
│  - tournament_players table              │
│  - users table                           │
└──────────────────────────────────────────┘
```

## Technology Stack

```
┌─────────────────────────────────────────┐
│   Framework: Spring Boot 4.0.3          │
│   Language: Java 21                     │
│   Build Tool: Maven 3.x                 │
└─────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────┐
│   Spring Security                       │
│   - HTTP Basic Authentication           │
│   - Role-based authorization            │
│   - SecurityFilterChain                 │
└─────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────┐
│   Spring Data JDBC                      │
│   - CrudRepository                      │
│   - @Table, @Id annotations             │
│   - Automatic query generation          │
└─────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────┐
│   Database: MySQL 8.x                   │
│   - Connection pooling (HikariCP)       │
│   - JDBC Driver: mysql-connector-java   │
└─────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────┐
│   Testing: JUnit 5 + Mockito            │
│   - Unit tests                          │
│   - Integration tests (Testcontainers) │
└─────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────┐
│   Utilities                             │
│   - Lombok (boilerplate reduction)      │
│   - Jackson (JSON serialization)        │
│   - Spring Boot Actuator (monitoring)   │
└─────────────────────────────────────────┘
```

## Validation Flow

```
┌─────────────────────────────────────────┐
│  Request arrives at Service Layer       │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Input Validation                       │
│  ✓ Required fields present              │
│  ✓ Field types correct                  │
│  ✓ String fields not empty              │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Business Rule Validation               │
│  ✓ Tournament name unique               │
│  ✓ Owner/user exists in DB              │
│  ✓ User has correct role                │
│  ✓ No duplicate assignments             │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Database Constraints                   │
│  ✓ Foreign key constraints              │
│  ✓ Unique constraints                   │
│  ✓ Not null constraints                 │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Success: Return DTO with data          │
│  Failure: Return error message          │
└─────────────────────────────────────────┘
```

## Error Handling Strategy

```
Exception Occurs
     │
     ▼
┌─────────────────────────────────────────┐
│  Service Layer Catches                  │
│  - Validates and returns error DTO      │
│  - TournamentResponse(false, "message") │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Controller Translates to HTTP          │
│  - 400 Bad Request (validation errors)  │
│  - 404 Not Found (resource missing)     │
│  - 401 Unauthorized (auth failed)       │
│  - 403 Forbidden (insufficient perms)   │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Client Receives JSON Response          │
│  { "success": false, "message": "..." } │
└─────────────────────────────────────────┘
```

---

*Architecture designed for scalability, maintainability, and security*

