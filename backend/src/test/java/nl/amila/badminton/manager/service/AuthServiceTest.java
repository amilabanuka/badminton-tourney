package nl.amila.badminton.manager.service;

import nl.amila.badminton.manager.dto.SignupRequest;
import nl.amila.badminton.manager.dto.LoginRequest;
import nl.amila.badminton.manager.dto.AuthResponse;
import nl.amila.badminton.manager.entity.Role;
import nl.amila.badminton.manager.entity.User;
import nl.amila.badminton.manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    private AuthService authService;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    public void testSignupSuccess() {
        // Arrange
        SignupRequest request = new SignupRequest(
            "testuser",
            "test@example.com",
            "password123",
            "Test",
            "User"
        );
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        User savedUser = new User("testuser", "test@example.com", "encodedPassword", "Test", "User");
        savedUser.setId(1L);
        savedUser.setRole(Role.PLAYER);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        AuthResponse response = authService.signup(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("User registered successfully", response.getMessage());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("PLAYER", response.getUser().getRole());
    }

    @Test
    public void testSignupWithDuplicateUsername() {
        // Arrange
        SignupRequest request = new SignupRequest("duplicate", "test@example.com", "password123", "Test", "User");
        when(userRepository.existsByUsername("duplicate")).thenReturn(true);

        // Act
        AuthResponse response = authService.signup(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Username already exists", response.getMessage());
    }

    @Test
    public void testSignupWithWeakPassword() {
        // Arrange
        SignupRequest request = new SignupRequest("user", "test@example.com", "123", "Test", "User");

        // Act
        AuthResponse response = authService.signup(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Password must be at least 6 characters", response.getMessage());
    }

    @Test
    public void testLoginSuccess() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "password123");
        User user = new User("testuser", "test@example.com", "encodedPassword", "Test", "User");
        user.setId(1L);
        user.setRole(Role.PLAYER);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("PLAYER", response.getUser().getRole());
    }

    @Test
    public void testLoginWithWrongPassword() {
        // Arrange
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        User user = new User("testuser", "test@example.com", "encodedPassword", "Test", "User");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Invalid username or password", response.getMessage());
    }

    @Test
    public void testLoginWithNonexistentUser() {
        // Arrange
        LoginRequest request = new LoginRequest("nonexistent", "password123");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Invalid username or password", response.getMessage());
    }
}

