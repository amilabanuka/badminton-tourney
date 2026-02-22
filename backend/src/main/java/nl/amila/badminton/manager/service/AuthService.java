package nl.amila.badminton.manager.service;

import nl.amila.badminton.manager.dto.SignupRequest;
import nl.amila.badminton.manager.dto.LoginRequest;
import nl.amila.badminton.manager.dto.AuthResponse;
import nl.amila.badminton.manager.entity.Role;
import nl.amila.badminton.manager.entity.User;
import nl.amila.badminton.manager.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user
     */
    public AuthResponse signup(SignupRequest request) {
        // Validate input
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return new AuthResponse(false, "Username is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return new AuthResponse(false, "Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return new AuthResponse(false, "Password is required");
        }
        if (request.getPassword().length() < 6) {
            return new AuthResponse(false, "Password must be at least 6 characters");
        }

        // Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return new AuthResponse(false, "Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse(false, "Email already exists");
        }

        // Create new user
        User user = new User(
            request.getUsername(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            request.getFirstName(),
            request.getLastName()
        );

        // Set role from request or default to PLAYER
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            user.setRole(Role.valueOf(request.getRole()));
        }

        User savedUser = userRepository.save(user);

        // Return success response with user data
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            savedUser.getRole()
        );

        return new AuthResponse(
            true,
            "User registered successfully",
            userDto,
            null
        );
    }

    /**
     * Authenticate user and return response
     */
    public AuthResponse login(LoginRequest request) {
        // Validate input
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return new AuthResponse(false, "Username is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return new AuthResponse(false, "Password is required");
        }

        // Find user by username
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            return new AuthResponse(false, "Invalid username or password");
        }

        User user = userOpt.get();

        // Check if user is enabled
        if (!user.isEnabled()) {
            return new AuthResponse(false, "User account is disabled");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new AuthResponse(false, "Invalid username or password");
        }

        // Return success response with user data
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole()
        );

        return new AuthResponse(
            true,
            "Login successful",
            userDto,
            null
        );
    }

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}

