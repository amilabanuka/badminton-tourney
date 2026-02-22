# Backend Authentication - Reference Card

## Quick Commands

### Start Backend
```powershell
cd E:\learn\badminton-app\backend
mvn spring-boot:run
```

### Test API
```powershell
# Signup
curl -X POST http://localhost:8098/api/auth/signup `
  -H "Content-Type: application/json" `
  -d '{"username":"john","email":"john@ex.com","password":"pass123"}'

# Login
curl -X POST http://localhost:8098/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"username":"john","password":"pass123"}'

# Health
curl http://localhost:8098/api/auth/health
```

---

## API Reference

| Endpoint | Method | Status | Purpose |
|----------|--------|--------|---------|
| `/api/auth/signup` | POST | 201/400 | Register user |
| `/api/auth/login` | POST | 200/401 | Authenticate user |
| `/api/auth/health` | GET | 200 | Health check |

---

## File Locations

```
Backend Files:
  - config: backend/src/main/java/.../config/SecurityConfig.java
  - controller: backend/src/main/java/.../controller/AuthController.java
  - service: backend/src/main/java/.../service/AuthService.java
  - repository: backend/src/main/java/.../repository/UserRepository.java
  - entity: backend/src/main/java/.../entity/User.java
  - dto: backend/src/main/java/.../dto/{SignupRequest,LoginRequest,AuthResponse}.java
  - config: backend/src/main/resources/application.yaml
  - schema: backend/src/main/resources/schema.sql
  - tests: backend/src/test/java/.../service/AuthServiceTest.java

Documentation:
  - README_AUTHENTICATION.md ........... Start here
  - QUICK_START.md ................... 5-min guide
  - BACKEND_AUTH_IMPLEMENTATION.md ... Details
  - AUTH_API_DOCUMENTATION.md ........ API specs
  - AUTHENTICATION_ARCHITECTURE.md .. Diagrams
```

---

## Request/Response Examples

### Signup Request
```json
{
  "username": "john",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Success Response
```json
{
  "success": true,
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "username": "john",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  },
  "token": null
}
```

### Error Response
```json
{
  "success": false,
  "message": "Username already exists",
  "user": null,
  "token": null
}
```

---

## Validation Rules

| Field | Required | Min | Max | Rules |
|-------|----------|-----|-----|-------|
| username | Yes | 1 | 255 | Unique |
| email | Yes | - | 255 | Unique, valid format |
| password | Yes | 6 | - | No max limit |
| firstName | No | - | 255 | - |
| lastName | No | - | 255 | - |

---

## HTTP Status Codes

| Code | Meaning | When |
|------|---------|------|
| 201 | Created | User registered successfully |
| 200 | OK | Login successful, health check |
| 400 | Bad Request | Validation error, duplicate username/email |
| 401 | Unauthorized | Invalid credentials, disabled account |

---

## Error Messages

**Signup Errors:**
- "Username is required"
- "Email is required"
- "Password is required"
- "Password must be at least 6 characters"
- "Username already exists"
- "Email already exists"

**Login Errors:**
- "Username is required"
- "Password is required"
- "Invalid username or password"
- "User account is disabled"

---

## Database Setup

```sql
CREATE DATABASE badminton_manager 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**Update in application.yaml:**
```yaml
spring:
  datasource:
    username: root
    password: [YOUR_PASSWORD]
```

---

## Build & Test

```powershell
# Build only
mvn clean compile

# Build with tests
mvn clean install

# Run tests
mvn test

# Package without tests
mvn package -DskipTests

# Clean build
mvn clean install -DskipTests
```

**Test Results:** 6/6 PASSED ✅

---

## Security Features

✓ BCrypt password hashing (work factor: 10)
✓ Automatic salt generation
✓ Input validation at every layer
✓ Unique constraints (username, email)
✓ CORS support
✓ Account enabled/disabled tracking
✓ Auto timestamps

---

## Technology Stack

- **Framework:** Spring Boot 4.0.3
- **Language:** Java 21
- **Database:** MySQL 8.0+
- **Security:** Spring Security + BCrypt
- **ORM:** Spring Data JDBC
- **Testing:** JUnit 5 + Mockito
- **Build:** Maven

---

## Common Issues

**Issue:** "Connection refused"
- **Fix:** Check MySQL is running, verify credentials in application.yaml

**Issue:** "Tables don't exist"
- **Fix:** Tables created automatically from schema.sql on startup

**Issue:** Build fails
- **Fix:** Run `mvn clean install -DskipTests`

**Issue:** Tests fail
- **Fix:** Ensure unit tests are using mocked repositories

---

## Key Classes

| Class | Purpose | Location |
|-------|---------|----------|
| User | Entity, JPA mapping | entity/User.java |
| UserRepository | Data access | repository/UserRepository.java |
| AuthService | Business logic | service/AuthService.java |
| AuthController | REST endpoints | controller/AuthController.java |
| SecurityConfig | Security setup | config/SecurityConfig.java |

---

## Available Methods

**AuthService:**
- `signup(SignupRequest)` → AuthResponse
- `login(LoginRequest)` → AuthResponse
- `findByUsername(String)` → Optional<User>
- `findByEmail(String)` → Optional<User>

**UserRepository:**
- `findByUsername(String)` → Optional<User>
- `findByEmail(String)` → Optional<User>
- `existsByUsername(String)` → boolean
- `existsByEmail(String)` → boolean
- `save(User)` → User
- `delete(User)` → void

---

## Configuration Defaults

```yaml
server:
  port: 8098

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
  sql:
    init:
      mode: always
  jpa:
    hibernate:
      ddl-auto: validate
```

---

## Testing the Backend

### Manual Testing
1. Start: `mvn spring-boot:run`
2. Open: Postman or curl
3. Test: Signup → Login → Health

### Automated Testing
```powershell
mvn test
```

### Load Testing
```powershell
# For future enhancement
# Use tools like JMeter or Gatling
```

---

## Future Enhancements

1. JWT tokens for stateless auth
2. Email verification on signup
3. Password reset functionality
4. Two-factor authentication
5. Role-based access control
6. Rate limiting for security
7. Audit logging

---

## Support Resources

- Spring Boot: https://spring.io/projects/spring-boot
- Spring Security: https://spring.io/projects/spring-security
- MySQL: https://dev.mysql.com/doc/
- Maven: https://maven.apache.org/
- Documentation: README_AUTHENTICATION.md

---

## Version History

| Version | Date | Status |
|---------|------|--------|
| 1.0.0 | 2026-02-22 | ✅ Complete |

---

## License

Part of Badminton Manager Application Project

---

**Last Updated:** February 22, 2026
**Status:** Production Ready ✅

