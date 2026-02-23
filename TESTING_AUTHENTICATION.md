# Testing the Authentication Fix

## Prerequisites
Make sure you have:
1. A user registered in the database
2. Backend running on the configured port

## How to Test

### 1. Test Custom Login Endpoint (Already Working)
```bash
# Register a new user
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "role": "PLAYER"
  }'

# Login with the custom endpoint
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### 2. Test HTTP Basic Authentication (NOW FIXED)
```bash
# Access protected endpoints using HTTP Basic Auth
curl -u testuser:password123 http://localhost:8080/api/tournaments

# Expected: Should work now and return tournament list
# Before fix: Would return 401 Unauthorized with BadCredentialsException
```

### 3. Test with Invalid Credentials
```bash
# Try with wrong password
curl -u testuser:wrongpassword http://localhost:8080/api/tournaments

# Expected: 401 Unauthorized
```

### 4. Test Public Endpoints (Still Work)
```bash
# No authentication required
curl http://localhost:8080/api/auth/health

# Expected: "Auth service is running"
```

## What Changed

### Before
- ❌ HTTP Basic Authentication failed with `BadCredentialsException`
- ✓ Custom login endpoint worked fine
- ❌ No `UserDetailsService` to load users from database

### After
- ✓ HTTP Basic Authentication works correctly
- ✓ Custom login endpoint still works
- ✓ `CustomUserDetailsService` loads users from database
- ✓ Passwords properly verified using BCrypt
- ✓ Role-based access control enforced

## Troubleshooting

### If you still get BadCredentialsException:
1. Verify the user exists in the database
2. Ensure the password in the database is BCrypt-encoded
3. Check that the username matches exactly (case-sensitive)

### Example valid user registration:
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "password": "admin123",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }'

# Then test with:
curl -u admin:admin123 http://localhost:8080/api/tournaments
```

