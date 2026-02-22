# Backend Authentication Implementation - Quick Start Guide

## What Was Implemented

### 1. User Entity (`User.java`)
- Database entity mapped to the `users` table
- Fields: id, username, email, password (encrypted), firstName, lastName, enabled, createdAt, updatedAt
- Timestamps automatically set on creation
- Uses Lombok for getters/setters and no-args constructor

### 2. User Repository (`UserRepository.java`)
- Spring Data JDBC repository
- Methods:
  - `findByUsername(String)` - Find user by username
  - `findByEmail(String)` - Find user by email
  - `existsByUsername(String)` - Check if username exists
  - `existsByEmail(String)` - Check if email exists

### 3. DTOs (Data Transfer Objects)
- **SignupRequest.java** - Request payload for user registration (Lombok for boilerplate)
- **LoginRequest.java** - Request payload for user authentication (Lombok for boilerplate)
- **AuthResponse.java** - Unified response format with nested UserDto (Lombok for boilerplate)

### 4. Authentication Service (`AuthService.java`)
- **signup()** - Register new users with validation
  - Validates input (username, email, password)
  - Checks for duplicates
  - Encrypts password using BCrypt
  - Returns success/failure with user details
- **login()** - Authenticate users
  - Validates credentials
  - Verifies password with BCrypt
  - Checks if account is enabled
  - Returns user details on success

### 5. Authentication Controller (`AuthController.java`)
- **POST /api/auth/signup** - Register new user (returns 201 Created)
- **POST /api/auth/login** - Authenticate user (returns 200 OK or 401 Unauthorized)
- **GET /api/auth/health** - Health check endpoint

### 6. Security Configuration (`SecurityConfig.java`)
- BCryptPasswordEncoder bean for secure password hashing
- CORS configuration for frontend communication
- Password encoder is injected into AuthService

### 7. Database Schema (`schema.sql`)
- SQL script to create the users table
- Auto-executed on application startup
- Includes indexes on username and email for performance

### 8. Application Configuration (`application.yaml`)
- Updated with MySQL datasource configuration
- Database auto-initialization enabled
- Logging configuration for debugging

## File Structure

```
backend/src/main/java/nl/amila/badminton/manager/
├── config/
│   └── SecurityConfig.java
├── controller/
│   └── AuthController.java
├── dto/
│   ├── SignupRequest.java
│   ├── LoginRequest.java
│   └── AuthResponse.java
├── entity/
│   └── User.java
├── repository/
│   └── UserRepository.java
├── service/
│   └── AuthService.java
└── Application.java

backend/src/main/resources/
├── application.yaml
└── schema.sql
```

## Setup Instructions

### 0. Lombok + Java 21 Prereqs
- Ensure your IDE enables annotation processing for Lombok.
- Project targets Java 21 (see `backend/pom.xml`).

### 1. Database Preparation
```sql
-- Create database (if not exists)
CREATE DATABASE badminton_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Update Database Credentials
Edit `backend/src/main/resources/application.yaml` and set:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/badminton_manager?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
    username: root
    password: [your-mysql-password]
```

### 3. Build the Project
```powershell
cd E:\learn\badminton-app\backend
mvn clean install
```

### 4. Run the Application
```powershell
mvn spring-boot:run
```

The backend will start on `http://localhost:8098`

## API Quick Reference

### Signup
```bash
POST http://localhost:8098/api/auth/signup
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Login
```bash
POST http://localhost:8098/api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

### Health Check
```bash
GET http://localhost:8098/api/auth/health
```

## Security Features Implemented

✅ **Password Encryption** - BCrypt hashing algorithm
✅ **Input Validation** - Username, email, password validation
✅ **Duplicate Prevention** - Unique username and email constraints
✅ **CORS Support** - Enabled for frontend communication
✅ **Account Status** - User enabled/disabled flag
✅ **Timestamps** - Automatic createdAt and updatedAt tracking

## Next Steps (Optional Enhancements)

1. **JWT Authentication** - Add JWT tokens for stateless authentication
2. **Email Verification** - Send confirmation emails on signup
3. **Password Reset** - Implement forgot password flow
4. **2FA** - Two-factor authentication
5. **Role-Based Access Control** - User roles and permissions
6. **Rate Limiting** - Prevent brute force attacks
7. **Audit Logging** - Log authentication events

## Troubleshooting

### Build Fails with Database Errors
- Ensure MySQL is running
- Check database credentials in application.yaml
- Verify the database name is created

### Password Encoding Not Working
- Confirm PasswordEncoder bean is in SecurityConfig.java
- Check that BCryptPasswordEncoder is being used

### CORS Errors from Frontend
- CORS is already configured for all origins
- If issues persist, check browser console for specific error messages

## Files Modified

- `backend/src/main/resources/application.yaml` - Updated with database configuration

## Files Created

✅ User.java - User entity (Lombok annotations)
✅ UserRepository.java - Repository interface
✅ SignupRequest.java - Signup DTO (Lombok annotations)
✅ LoginRequest.java - Login DTO (Lombok annotations)
✅ AuthResponse.java - Response DTO with UserDto (Lombok annotations)
✅ AuthService.java - Authentication business logic
✅ AuthController.java - REST endpoints
✅ SecurityConfig.java - Security configuration
✅ schema.sql - Database schema
✅ AUTH_API_DOCUMENTATION.md - Detailed API documentation

## Build Status

✅ **BUILD SUCCESS** - All 9 source files compiled without errors

The implementation is complete and ready to use!
