# Quick Start - Backend Authentication

## 🚀 Get Started in 5 Minutes

### Step 1: Set Up Database
```powershell
# Create database using MySQL CLI or tool
CREATE DATABASE badminton_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Step 2: Update Database Credentials
Edit `backend/src/main/resources/application.yaml`:
```yaml
spring:
  datasource:
    username: root
    password: [YOUR_MYSQL_PASSWORD]
```

### Step 3: Start Backend
```powershell
cd E:\learn\badminton-app\backend
mvn spring-boot:run
```

Backend runs on: `http://localhost:8098`

---

## 📌 API Quick Reference

### Sign Up
```powershell
$body = @{
    username = "john"
    email = "john@example.com"
    password = "password123"
    firstName = "John"
    lastName = "Doe"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8098/api/auth/signup" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

### Login
```powershell
$body = @{
    username = "john"
    password = "password123"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8098/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $body
```

### Check Health
```powershell
Invoke-WebRequest -Uri "http://localhost:8098/api/auth/health"
```

---

## 📂 Key Files

| File | Purpose |
|------|---------|
| `entity/User.java` | User database model |
| `repository/UserRepository.java` | Database access |
| `service/AuthService.java` | Business logic |
| `controller/AuthController.java` | REST endpoints |
| `config/SecurityConfig.java` | Security setup |
| `schema.sql` | Database schema |
| `application.yaml` | Configuration |

---

## ✅ What's Included

✅ User registration with validation
✅ User login with password verification
✅ BCrypt password encryption
✅ Input validation (username, email, password)
✅ Duplicate username/email prevention
✅ Account status tracking
✅ Auto-timestamps (created/updated)
✅ CORS support for frontend
✅ Unit tests with Mockito
✅ MySQL database integration

---

## 🔍 Response Format

All endpoints return JSON:
```json
{
  "success": true/false,
  "message": "Description",
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

---

## 🧪 Run Tests

```powershell
cd E:\learn\badminton-app\backend
mvn test
```

**Expected Result:** 6 tests in AuthServiceTest pass

---

## 📚 Full Documentation

- `AUTHENTICATION_SUMMARY.md` - Complete overview
- `BACKEND_AUTH_IMPLEMENTATION.md` - Implementation details
- `AUTH_API_DOCUMENTATION.md` - Detailed API docs

---

## ⚠️ Common Issues

**Issue:** Connection refused
- **Fix:** Ensure MySQL is running and credentials are correct

**Issue:** Table doesn't exist
- **Fix:** Database will be created automatically on startup

**Issue:** Password encoding fails
- **Fix:** Ensure PasswordEncoder bean is loaded in SecurityConfig

---

## 🎯 Next Steps

1. ✅ Backend authentication working
2. ⬜ Connect frontend to backend
3. ⬜ Add JWT token support (optional)
4. ⬜ Add email verification (optional)

---

**Status:** Ready to use ✅

