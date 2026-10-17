package dev.portfolio.taskflow.service;

import dev.portfolio.taskflow.dto.request.LoginRequest;
import dev.portfolio.taskflow.dto.request.RegisterRequest;
import dev.portfolio.taskflow.dto.response.AuthResponse;
import dev.portfolio.taskflow.enums.Role;
import dev.portfolio.taskflow.model.User;
import dev.portfolio.taskflow.repository.UserRepository;
import dev.portfolio.taskflow.support.BaseIntegrationTest;
import dev.portfolio.taskflow.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.*;

class AuthServiceTest extends BaseIntegrationTest {

    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    @Autowired TestDataFactory factory;

    @Test
    void register_shouldCreateUserAndReturnToken() {
        var request = new RegisterRequest("alice", "alice@test.com", "secret123", "Alice");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isNotBlank();
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.role()).isEqualTo("USER");

        User saved = userRepository.findByUsername("alice").orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo("secret123"); // зашифрован
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void register_duplicateUsername_shouldFail() {
        factory.createUser("bob");
        var request = new RegisterRequest("bob", "bob2@test.com", "secret123", null);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void register_duplicateEmail_shouldFail() {
        User existing = factory.createUser("charlie");
        var request = new RegisterRequest("other", existing.getEmail(), "secret123", null);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        factory.createUser("dave");
        var response = authService.login(new LoginRequest("dave", "password"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.username()).isEqualTo("dave");
    }

    @Test
    void login_wrongPassword_shouldThrow() {
        factory.createUser("eve");
        assertThatThrownBy(() -> authService.login(new LoginRequest("eve", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }
}