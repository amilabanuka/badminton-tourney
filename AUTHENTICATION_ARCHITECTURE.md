# Authentication Architecture & Flow

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend (Vue 3)                       │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Signup/Login Forms                                 │  │
│  │  (sends credentials via HTTP)                       │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ HTTP Requests
                       │ (Port 8080 -> 8098 via proxy)
                       │
┌──────────────────────▼──────────────────────────────────────┐
│                  Backend API (Spring Boot)                  │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │          AuthController (Port 8098)                 │  │
│  │  POST /api/auth/signup                              │  │
│  │  POST /api/auth/login                               │  │
│  │  GET  /api/auth/health                              │  │
│  └──────────────────────────────────────────────────────┘  │
│                       │                                    │
│  ┌────────────────────▼──────────────────────────────────┐ │
│  │         AuthService (Business Logic)                 │ │
│  │  - Input validation                                  │ │
│  │  - Duplicate checking                                │ │
│  │  - Password encryption/verification                 │ │
│  └────────────────────┬──────────────────────────────────┘ │
│                       │                                    │
│  ┌────────────────────▼──────────────────────────────────┐ │
│  │   UserRepository (Data Access Layer)                 │ │
│  │  - Spring Data JDBC                                  │ │
│  │  - findByUsername(), findByEmail()                   │ │
│  │  - existsByUsername(), existsByEmail()               │ │
│  └────────────────────┬──────────────────────────────────┘ │
│                       │                                    │
└───────────────────────┼────────────────────────────────────┘
                        │
                        │ JDBC Driver
                        │
┌───────────────────────▼────────────────────────────────────┐
│              MySQL Database (Port 3306)                    │
│                                                            │
│  ┌────────────────────────────────────────────────────┐   │
│  │  users table                                       │   │
│  │  ┌─────┬─────────┬────────────┬──────────────┐    │   │
│  │  │ id  │ username│ email      │ password     │... │   │
│  │  ├─────┼─────────┼────────────┼──────────────┤    │   │
│  │  │ 1   │ john    │ john@ex.cm │ $2a$10$... │... │   │
│  │  │ 2   │ jane    │ jane@ex.cm │ $2a$10$... │... │   │
│  │  └─────┴─────────┴────────────┴──────────────┘    │   │
│  └────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────┘
```

## Signup Flow

```
┌─────────────┐
│   User      │
│  Submits    │
│  Form       │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────┐
│  Frontend validates locally     │
│  (client-side checks)           │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  POST /api/auth/signup          │
│  {username, email, password}    │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  AuthController                 │
│  receives request               │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  AuthService.signup()           │
│  ├─ Validate input              │
│  │  ├─ Check required fields    │
│  │  └─ Check password length    │
│  │                              │
│  ├─ Check duplicates            │
│  │  ├─ findByUsername()         │
│  │  └─ findByEmail()            │
│  │                              │
│  ├─ Encrypt password            │
│  │  └─ BCryptPasswordEncoder    │
│  │                              │
│  └─ Save user to database       │
│     └─ userRepository.save()    │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  Return AuthResponse            │
│  {success: true, user: {...}}   │
│  Status: 201 Created            │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  Frontend receives response     │
│  - Store user info              │
│  - Show success message         │
│  - Redirect to home             │
└─────────────────────────────────┘
```

## Login Flow

```
┌─────────────┐
│   User      │
│  Enters     │
│  Credentials│
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────┐
│  POST /api/auth/login           │
│  {username, password}           │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  AuthController                 │
│  receives request               │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  AuthService.login()            │
│  ├─ Validate input              │
│  │  ├─ Check username exists    │
│  │  └─ Check password exists    │
│  │                              │
│  ├─ Find user by username       │
│  │  └─ userRepository           │
│  │     .findByUsername()        │
│  │                              │
│  ├─ User found?                 │
│  │  ├─ YES: Continue            │
│  │  └─ NO: Return error (401)   │
│  │                              │
│  ├─ Check if enabled            │
│  │  ├─ YES: Continue            │
│  │  └─ NO: Return error (401)   │
│  │                              │
│  ├─ Verify password             │
│  │  ├─ BCrypt.matches()         │
│  │  ├─ Password matches?        │
│  │  │  ├─ YES: Continue         │
│  │  │  └─ NO: Return error (401)│
│  │  │                           │
│  │                              │
│  └─ Return user details         │
│     └─ (without password)       │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  Return AuthResponse            │
│  {success: true, user: {...}}   │
│  Status: 200 OK                 │
└──────┬──────────────────────────┘
       │
       ▼
┌─────────────────────────────────┐
│  Frontend receives response     │
│  - Store user session           │
│  - Show dashboard               │
│  - Enable protected features    │
└─────────────────────────────────┘
```

## Error Handling Flow

```
Validation Errors:
  • Username empty → 400 Bad Request
  • Email empty → 400 Bad Request
  • Password < 6 chars → 400 Bad Request

Duplicate Errors:
  • Username exists → 400 Bad Request
  • Email exists → 400 Bad Request

Login Errors:
  • User not found → 401 Unauthorized
  • Password incorrect → 401 Unauthorized
  • Account disabled → 401 Unauthorized
```

## Database Schema

```sql
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  
  -- User identifiers (must be unique)
  username VARCHAR(255) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  
  -- Credentials (password is encrypted)
  password VARCHAR(255) NOT NULL,  -- BCrypt hash
  
  -- Profile information
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  
  -- Account status
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  
  -- Timestamps (milliseconds since epoch)
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL,
  
  -- Indexes for fast lookup
  INDEX idx_username (username),
  INDEX idx_email (email)
);
```

## Security Measures

```
┌──────────────────────────────────────────────┐
│  Frontend (Client-Side)                      │
│  ├─ Validate input format                   │
│  ├─ Show/hide password toggle               │
│  └─ No sensitive data in localStorage       │
└──────────────────────────────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────────┐
│  Network (HTTPS in production)               │
│  └─ Encrypt data in transit                 │
└──────────────────────────────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────────┐
│  Backend API (Spring Boot)                   │
│  ├─ Input validation (required fields)       │
│  ├─ Business logic validation                │
│  ├─ CORS protection                          │
│  └─ Error responses don't leak info          │
└──────────────────────────────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────────┐
│  Password Encryption (BCrypt)                │
│  ├─ Salt: automatically included             │
│  ├─ Work factor: 10 (2^10 iterations)       │
│  ├─ One-way hashing                         │
│  └─ Resistant to rainbow tables             │
└──────────────────────────────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────────┐
│  Database (MySQL)                            │
│  ├─ Never store passwords in plain text      │
│  ├─ Store BCrypt hashes only                │
│  ├─ Unique constraints on username/email     │
│  └─ Indexed for performance                 │
└──────────────────────────────────────────────┘
```

## Class Relationships

```
┌──────────────────────┐
│   AuthController     │
│  ┌────────────────┐  │
│  │ signup()       │  │
│  │ login()        │  │
│  │ health()       │  │
│  └────────────────┘  │
└──────────┬───────────┘
           │ uses
           ▼
┌──────────────────────┐
│   AuthService        │
│  ┌────────────────┐  │
│  │ signup()       │  │
│  │ login()        │  │
│  │ findByXxx()    │  │
│  └────────────────┘  │
└──────────┬───────────┘
      ┌────┴─────┐
      │ uses      │ uses
      ▼           ▼
┌──────────────────────────────────────────┐
│   UserRepository         PasswordEncoder  │
│  ┌────────────────────┐  ┌────────────┐  │
│  │ findByUsername()   │  │ encode()   │  │
│  │ findByEmail()      │  │ matches()  │  │
│  │ save()             │  └────────────┘  │
│  │ existsByXxx()      │                  │
│  └────────────────────┘                  │
└────────────┬──────────────────────────────┘
             │ uses
             ▼
    ┌──────────────────┐
    │  User Entity     │
    │  ┌────────────┐  │
    │  │ id         │  │
    │  │ username   │  │
    │  │ email      │  │
    │  │ password   │  │
    │  │ ...        │  │
    │  └────────────┘  │
    └──────────────────┘
```

## Request/Response Flow

```
Frontend                          Backend
   │                               │
   ├─ POST /api/auth/signup ──────>│
   │  SignupRequest JSON            │
   │  {                             │
   │    username: "john",          │
   │    email: "john@ex.com",      │
   │    password: "pass123",       │
   │    firstName: "John",         │
   │    lastName: "Doe"            │
   │  }                            │
   │                               │
   │<────── 201 Created ───────────┤
   │  AuthResponse JSON             │
   │  {                             │
   │    success: true,             │
   │    message: "...",            │
   │    user: {                    │
   │      id: 1,                   │
   │      username: "john",        │
   │      email: "john@ex.com",    │
   │      firstName: "John",       │
   │      lastName: "Doe"          │
   │    },                         │
   │    token: null                │
   │  }                            │
   │                               │
   │                               │
   │ POST /api/auth/login ────────>│
   │  LoginRequest JSON             │
   │  {                             │
   │    username: "john",          │
   │    password: "pass123"        │
   │  }                            │
   │                               │
   │<────── 200 OK ────────────────┤
   │  AuthResponse JSON             │
   │  {                             │
   │    success: true,             │
   │    message: "...",            │
   │    user: {...},               │
   │    token: null                │
   │  }                            │
```

---

This architecture provides a clean separation of concerns with proper security measures and validation at each layer.

