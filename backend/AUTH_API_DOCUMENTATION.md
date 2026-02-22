# Authentication API Documentation

## Overview
This document describes the signup and login functionality implemented in the badminton-app backend.

## Database Schema

### Users Table
The `users` table stores user credentials and profile information:

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL (BCrypt encrypted),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at BIGINT NOT NULL (timestamp in ms),
    updated_at BIGINT NOT NULL (timestamp in ms),
    INDEX idx_username (username),
    INDEX idx_email (email)
);
```

## API Endpoints

### 1. Signup (Register New User)
**Endpoint:** `POST /api/auth/signup`

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (Success - 201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  },
  "token": null
}
```

**Response (Failure - 400 Bad Request):**
```json
{
  "success": false,
  "message": "Username already exists",
  "user": null,
  "token": null
}
```

**Validation Rules:**
- Username: Required, must be unique
- Email: Required, must be unique, valid email format
- Password: Required, minimum 6 characters
- firstName: Optional
- lastName: Optional

**Possible Error Messages:**
- "Username is required"
- "Email is required"
- "Password is required"
- "Password must be at least 6 characters"
- "Username already exists"
- "Email already exists"

---

### 2. Login
**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "securePassword123"
}
```

**Response (Success - 200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  },
  "token": null
}
```

**Response (Failure - 401 Unauthorized):**
```json
{
  "success": false,
  "message": "Invalid username or password",
  "user": null,
  "token": null
}
```

**Validation Rules:**
- Username: Required
- Password: Required

**Possible Error Messages:**
- "Username is required"
- "Password is required"
- "Invalid username or password"
- "User account is disabled"

---

### 3. Auth Health Check
**Endpoint:** `GET /api/auth/health`

**Response (200 OK):**
```plain
Auth service is running
```

## Project Structure

```
backend/src/main/java/nl/amila/badminton/manager/
├── config/
│   └── SecurityConfig.java          # Spring Security & Password Encoder configuration
├── controller/
│   └── AuthController.java          # REST endpoints for signup and login
├── dto/
│   ├── SignupRequest.java           # Signup request payload
│   ├── LoginRequest.java            # Login request payload
│   └── AuthResponse.java            # Unified response format (includes UserDto)
├── entity/
│   └── User.java                    # User JPA entity for database persistence
├── repository/
│   └── UserRepository.java          # Spring Data JDBC repository
├── service/
│   └── AuthService.java             # Business logic for authentication
└── Application.java                 # Spring Boot main application class
```

## Implementation Details

### Security
- **Password Encryption:** BCrypt with Spring Security PasswordEncoder
- **CORS:** Enabled for all origins (configurable in SecurityConfig)
- **Database:** MySQL with JDBC configuration

### User Entity (nl.amila.badminton.manager.entity.User)
- Fields: id, username, email, password (encrypted), firstName, lastName, enabled, createdAt, updatedAt
- Auto-timestamps enabled (createdAt and updatedAt set on instantiation)
- Unique constraints on username and email

### AuthService (nl.amila.badminton.manager.service.AuthService)
- Validates user input
- Encodes passwords using BCryptPasswordEncoder
- Checks for duplicate usernames/emails
- Authenticates users with password verification
- Returns standardized AuthResponse

### AuthController (nl.amila.badminton.manager.controller.AuthController)
- POST /api/auth/signup - Register new user
- POST /api/auth/login - Authenticate user
- GET /api/auth/health - Service health check
- Proper HTTP status codes (201 for created, 200 for success, 400 for validation errors, 401 for auth failures)

## Database Setup

1. Create MySQL database:
```sql
CREATE DATABASE badminton_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Update `application.yaml` with your database credentials:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/badminton_manager?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
    username: root
    password: [your-password]
    driver-class-name: com.mysql.cj.jdbc.Driver
```

3. The schema will be created automatically via `schema.sql` on application startup.

## Running the Application

1. Ensure MySQL is running
2. Update database credentials in `application.yaml`
3. Build the backend:
```bash
cd backend
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

5. The backend will be available at `http://localhost:8098`

## Testing the API

### Using curl:

**Signup:**
```bash
curl -X POST http://localhost:8098/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8098/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

**Health Check:**
```bash
curl http://localhost:8098/api/auth/health
```

### Using Postman:
1. Create POST request to `http://localhost:8098/api/auth/signup`
2. Set Body to raw JSON
3. Enter the signup payload
4. Send the request

Repeat for login endpoint.

## Future Enhancements

1. **JWT Tokens:** Add JWT token generation for stateless authentication
2. **Refresh Tokens:** Implement token refresh mechanism
3. **Email Verification:** Send verification emails on signup
4. **Password Reset:** Implement forgot password functionality
5. **Two-Factor Authentication:** Add 2FA support
6. **Role-Based Access Control:** Implement user roles and permissions
7. **Account Lockout:** Lock accounts after failed login attempts
8. **Audit Logging:** Log authentication events

## Troubleshooting

### Database Connection Issues
- Ensure MySQL is running
- Check database credentials in `application.yaml`
- Verify database name matches the URL

### Password Encoding Issues
- Ensure `PasswordEncoder` bean is properly configured in SecurityConfig
- BCrypt encoding may take a few milliseconds, so ensure your timeout is sufficient

### Duplicate Key Errors
- Username and email must be unique
- Check for existing users with same username/email before signup

### CORS Errors
- CORS is enabled for all origins in SecurityConfig
- If issues persist, check browser console for specific CORS headers needed

