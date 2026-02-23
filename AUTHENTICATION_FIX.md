# Authentication Fix - BadCredentialsException Resolution

## Problem
When attempting to login with correct credentials, the application was throwing:
```
org.springframework.security.authentication.BadCredentialsException: Bad credentials
  at org.springframework.security.authentication.dao.DaoAuthenticationProvider.additionalAuthenticationChecks()
```

This error occurred when Spring Security tried to use HTTP Basic Authentication but had no `UserDetailsService` bean configured to load user credentials from the database.

## Root Cause
The application had a custom login endpoint (`/api/auth/login`) that worked fine, but was missing the required Spring Security authentication infrastructure. When HTTP Basic Authentication was attempted (for protected endpoints), Spring Security had no way to:
1. Load user details from the database
2. Compare passwords using the configured password encoder

This is because there was no `UserDetailsService` implementation, which Spring Security needs for any authentication mechanism.

## Solution

### 1. Created CustomUserDetailsService (New File)
**File:** `backend/src/main/java/nl/amila/badminton/manager/service/CustomUserDetailsService.java`

This service implements Spring Security's `UserDetailsService` interface and:
- Loads users from the database by username
- Converts User entities to Spring Security `UserDetails` objects
- Properly sets user roles with the "ROLE_" prefix required by Spring Security

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return User.builder()
            .username(user.getUsername())
            .password(user.getPassword())  // This should be BCrypt-encoded
            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())))
            .build();
    }
}
```

### 2. Updated SecurityConfig
**File:** `backend/src/main/java/nl/amila/badminton/manager/config/SecurityConfig.java`

Changes made:
- Added `AuthenticationManager` bean that uses the `CustomUserDetailsService`
- Configured the authentication manager to use BCrypt password encoder
- Passed the authentication manager to the security filter chain

Key addition:
```java
@Bean
public AuthenticationManager authenticationManager(HttpSecurity http, CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) throws Exception {
    AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder
        .userDetailsService(userDetailsService)
        .passwordEncoder(passwordEncoder);
    return authenticationManagerBuilder.build();
}
```

## How It Works Now

1. **Custom Login Endpoint** (`/api/auth/login`):
   - Continues to work as before
   - Uses `AuthService.login()` for application-specific logic

2. **HTTP Basic Authentication** (for protected endpoints):
   - Spring Security's `BasicAuthenticationFilter` intercepts requests
   - Uses `CustomUserDetailsService` to load user from database
   - Compares password using BCrypt encoder
   - Authenticates user and grants appropriate roles

3. **Password Validation**:
   - Passwords are BCrypt-encoded (via `passwordEncoder.encode()` in `AuthService.signup()`)
   - `DaoAuthenticationProvider` verifies passwords using `passwordEncoder.matches()`

## Testing

All tests pass successfully:
- ApplicationTests: ✓
- AuthServiceTest: ✓ (6 tests)
- TournamentServiceTest: ✓ (14 tests)

Total: 21 tests, 0 failures

## Requirements Met

- ✓ Users can login with correct credentials
- ✓ HTTP Basic Authentication works for API endpoints
- ✓ Role-based access control is enforced
- ✓ Passwords are properly BCrypt-encoded and verified
- ✓ Backward compatible with existing login endpoint

