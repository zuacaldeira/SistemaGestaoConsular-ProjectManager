package ao.gov.sgcd.pm.controller;

import ao.gov.sgcd.pm.config.JwtTokenProvider;
import ao.gov.sgcd.pm.config.LoginRateLimiter;
import ao.gov.sgcd.pm.config.UserProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthControllerTest.TestConfig.class)
@TestPropertySource(properties = {
        "sgcd-pm.stakeholder.token=test-stakeholder-token",
        "sgcd-pm.jwt.secret=test-secret-key-for-unit-testing-minimum-32-bytes",
        "sgcd-pm.jwt.expiration=86400000"
})
class AuthControllerTest {

    static class TestConfig {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public LoginRateLimiter loginRateLimiter() {
            return new LoginRateLimiter();
        }

        @Bean
        public UserProperties userProperties() {
            PasswordEncoder encoder = new BCryptPasswordEncoder();
            UserProperties props = new UserProperties();
            UserProperties.UserCredential admin = new UserProperties.UserCredential();
            admin.setUsername("admin");
            admin.setPasswordHash(encoder.encode("admin123"));
            admin.setRole("DEVELOPER");
            UserProperties.UserCredential stakeholder = new UserProperties.UserCredential();
            stakeholder.setUsername("stakeholder");
            stakeholder.setPasswordHash(encoder.encode("stakeholder2026"));
            stakeholder.setRole("STAKEHOLDER");
            props.setUsers(List.of(admin, stakeholder));
            return props;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_withValidAdminCredentials_shouldReturn200WithToken() throws Exception {
        when(tokenProvider.generateToken("admin", "DEVELOPER")).thenReturn("mock-jwt-token");
        when(tokenProvider.generateRefreshToken("admin", "DEVELOPER")).thenReturn("mock-refresh-token");
        when(tokenProvider.getExpiration()).thenReturn(86400000L);

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "admin", "password", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("mock-jwt-token")))
                .andExpect(jsonPath("$.refreshToken", is("mock-refresh-token")))
                .andExpect(jsonPath("$.role", is("DEVELOPER")))
                .andExpect(jsonPath("$.expiresIn", is(86400000)));
    }

    @Test
    void login_withValidStakeholderCredentials_shouldReturn200WithToken() throws Exception {
        when(tokenProvider.generateToken("stakeholder", "STAKEHOLDER")).thenReturn("mock-stakeholder-token");
        when(tokenProvider.generateRefreshToken("stakeholder", "STAKEHOLDER")).thenReturn("mock-refresh-token");
        when(tokenProvider.getExpiration()).thenReturn(86400000L);

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "stakeholder", "password", "stakeholder2026"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("mock-stakeholder-token")))
                .andExpect(jsonPath("$.refreshToken", is("mock-refresh-token")))
                .andExpect(jsonPath("$.role", is("STAKEHOLDER")))
                .andExpect(jsonPath("$.expiresIn", is(86400000)));
    }

    @Test
    void login_withInvalidCredentials_shouldReturn401() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "wrong", "password", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withWrongPassword_shouldReturn401() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "admin", "password", "wrongpassword"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withBlankUsername_shouldReturn400() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "", "password", "admin123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withBlankPassword_shouldReturn400() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "admin", "password", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withMissingBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void me_withoutAuthentication_shouldReturn401() throws Exception {
        // When no authentication is present, the controller returns 401
        mockMvc.perform(get("/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
