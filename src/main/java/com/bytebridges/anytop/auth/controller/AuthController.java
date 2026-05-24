package com.bytebridges.anytop.auth.controller;

import com.bytebridges.anytop.auth.dto.LoginRequest;
import com.bytebridges.anytop.auth.dto.LoginResponse;
import com.bytebridges.anytop.auth.dto.RegisterRequest;
import com.bytebridges.anytop.auth.entity.User;
import com.bytebridges.anytop.auth.enums.Role;
import com.bytebridges.anytop.auth.repository.UserRepository;
import com.bytebridges.anytop.auth.service.JwtService;
import com.bytebridges.anytop.common.ServiceResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Authentication Management", description = "APIs for user registration, authentication, and JWT token generation")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	// =========================
	// REGISTER
	// =========================

	@Operation(summary = "Register New User", description = "Registers a new user account with encrypted password")
	@PostMapping("/register")
	public ServiceResponse<?> register(@RequestBody RegisterRequest request) {

		// Check existing username
		boolean exists = userRepository.findByUsername(request.getUsername()).isPresent();

		if (exists) {

			return ServiceResponse.error("Username already exists");
		}

		// Create user
		User user = User.builder().username(request.getUsername())
				.password(passwordEncoder.encode(request.getPassword())).fullName(request.getFullName())
				.role(Role.OPERATOR).active(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

		userRepository.save(user);

		return ServiceResponse.success("User registered successfully");
	}

	// =========================
	// LOGIN
	// =========================

	@Operation(summary = "User Login", description = "Authenticates user credentials and returns JWT access token")
	@PostMapping("/login")
	public ServiceResponse<?> login(@RequestBody LoginRequest request) {

		// Find user
		User user = userRepository.findByUsername(request.getUsername())
				.orElseThrow(() -> new RuntimeException("Invalid username or password"));

		// Check active
		if (!Boolean.TRUE.equals(user.getActive())) {

			return ServiceResponse.error("User account is disabled");
		}

		// Verify password
		boolean passwordMatched = passwordEncoder.matches(request.getPassword(), user.getPassword());

		if (!passwordMatched) {

			return ServiceResponse.error("Invalid username or password");
		}

		// Generate JWT
		String token = jwtService.generateToken(user.getUsername());

		// Response DTO
		LoginResponse response = LoginResponse.builder().token(token).username(user.getUsername())
				.role(user.getRole().name()).build();

		return ServiceResponse.success(response);
	}
	
	@Operation(summary = "User Logout", description = "Logs out the current user")
	@PostMapping("/logout")
	public ServiceResponse<?> logout() {

		// JWT is stateless
		// Client should remove token from storage

		return ServiceResponse.success("Logout successful");
	}
}