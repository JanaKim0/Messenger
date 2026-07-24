package com.sitapp.service;

import com.sitapp.domain.Role;
import com.sitapp.domain.User;
import com.sitapp.domain.UserStatus;
import com.sitapp.repository.UserRepository;
import com.sitapp.security.JwtService;
import com.sitapp.web.dto.AuthResponse;
import com.sitapp.web.dto.LoginRequest;
import com.sitapp.web.dto.RegisterRequest;
import com.sitapp.web.dto.UserResponse;
import com.sitapp.web.error.ApiException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Creates a new PENDING account. No token is issued: the user cannot log in
     * until an administrator approves the registration.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw ApiException.conflict("Username is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("Email is already registered");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(Role.USER);
        user.setStatus(UserStatus.PENDING);

        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Authenticates credentials and returns a JWT. Rejects accounts that are not
     * yet approved with a clear message.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ApiException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (DisabledException ex) {
            throw switch (user.getStatus()) {
                case PENDING -> ApiException.forbidden("Your account is awaiting administrator approval");
                case REJECTED -> ApiException.forbidden("Your registration was rejected");
                default -> ApiException.forbidden("Your account is not active");
            };
        }

        String token = jwtService.generateToken(user);
        return AuthResponse.of(token, UserResponse.from(user));
    }
}
