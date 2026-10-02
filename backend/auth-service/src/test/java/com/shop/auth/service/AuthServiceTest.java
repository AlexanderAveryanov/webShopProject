package com.shop.auth.service;

import com.shop.auth.dto.AuthResponse;
import com.shop.auth.dto.LoginRequest;
import com.shop.auth.dto.RegisterRequest;
import com.shop.auth.dto.UserResponse;
import com.shop.auth.entity.Role;
import com.shop.auth.entity.User;
import com.shop.auth.exception.EmailAlreadyExistsException;
import com.shop.auth.exception.InvalidPasswordException;
import com.shop.auth.exception.UserNotFoundException;
import com.shop.auth.repository.UserRepository;
import com.shop.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUserAndReturnUserResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(" Test@Example.COM ");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setRole(Role.USER);
        savedUser.setCreatedAt(LocalDateTime.now());
        savedUser.setUpdatedAt(LocalDateTime.now());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getRole()).isEqualTo(Role.USER);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User captured = captor.getValue();
        assertThat(captured.getEmail()).isEqualTo("test@example.com");
        assertThat(captured.getPassword()).isEqualTo("encodedPassword");
        assertThat(captured.getFirstName()).isEqualTo("John");
        assertThat(captured.getLastName()).isEqualTo("Doe");
        assertThat(captured.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldThrowOnUniqueViolation23505() throws SQLException {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        PSQLException psql = new PSQLException("duplicate key", org.postgresql.util.PSQLState.UNIQUE_VIOLATION);
        DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint", psql);

        when(userRepository.save(any(User.class))).thenThrow(ex);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void register_shouldRethrowOtherDataIntegrityViolation() throws SQLException {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        PSQLException psql = new PSQLException("other", org.postgresql.util.PSQLState.FOREIGN_KEY_VIOLATION);
        DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint", psql);

        when(userRepository.save(any(User.class))).thenThrow(ex);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void login_shouldReturnAuthResponseWithToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail(" Test@Example.COM ");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.USER);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getType()).isEqualTo(AuthResponse.BEARER);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void login_shouldThrowWhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void login_shouldThrowWhenPasswordInvalid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void mapToUserResponse_shouldMapFields() {
        User user = new User();
        user.setId(5L);
        user.setEmail("map@example.com");
        user.setFirstName("A");
        user.setLastName("B");
        user.setRole(Role.ADMIN);

        UserResponse response = authService.mapToUserResponse(user);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getEmail()).isEqualTo("map@example.com");
        assertThat(response.getFirstName()).isEqualTo("A");
        assertThat(response.getLastName()).isEqualTo("B");
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void mapToAuthResponse_shouldMapFieldsWithoutToken() {
        User user = new User();
        user.setId(2L);
        user.setEmail("auth@example.com");
        user.setFirstName("F");
        user.setLastName("L");
        user.setRole(Role.USER);

        AuthResponse response = authService.mapToAuthResponse(user);

        assertThat(response.getType()).isEqualTo(AuthResponse.BEARER);
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getEmail()).isEqualTo("auth@example.com");
        assertThat(response.getFirstName()).isEqualTo("F");
        assertThat(response.getLastName()).isEqualTo("L");
        assertThat(response.getRole()).isEqualTo(Role.USER);
        assertThat(response.getToken()).isNull();
    }
}
