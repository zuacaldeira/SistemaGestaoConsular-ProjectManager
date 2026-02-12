package ao.gov.sgcd.pm.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider tokenProvider;

    // --- Public endpoints: should be accessible without authentication ---

    @Test
    void authLogin_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void authLogin_invalidCredentials_shouldReturn401() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"wrong\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void stakeholderDashboard_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v1/dashboard/stakeholder"))
                .andExpect(status().isOk());
    }

    @Test
    void stakeholderEndpoint_withoutToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/v1/stakeholder"))
                .andExpect(status().isForbidden());
    }

    @Test
    void stakeholderEndpoint_withValidToken_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/v1/stakeholder")
                        .param("token", "test-stakeholder-token"))
                .andExpect(status().isOk());
    }

    // --- Protected endpoints: should require authentication ---

    @Test
    void developerDashboard_withoutAuth_shouldReturn401Or403() throws Exception {
        mockMvc.perform(get("/v1/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sprints_withoutAuth_shouldReturn401Or403() throws Exception {
        mockMvc.perform(get("/v1/sprints"))
                .andExpect(status().isForbidden());
    }

    @Test
    void tasks_withoutAuth_shouldReturn401Or403() throws Exception {
        mockMvc.perform(get("/v1/tasks"))
                .andExpect(status().isForbidden());
    }

    @Test
    void reports_withoutAuth_shouldReturn401Or403() throws Exception {
        mockMvc.perform(get("/v1/reports"))
                .andExpect(status().isForbidden());
    }

    @Test
    void calendar_withoutAuth_shouldReturn401Or403() throws Exception {
        mockMvc.perform(get("/v1/calendar"))
                .andExpect(status().isForbidden());
    }

    @Test
    void prompts_withoutAuth_shouldReturn401Or403() throws Exception {
        mockMvc.perform(get("/v1/prompts/today"))
                .andExpect(status().isForbidden());
    }

    // --- Authenticated endpoints with valid JWT ---

    @Test
    void developerDashboard_withValidJwt_shouldReturn200() throws Exception {
        String token = tokenProvider.generateToken("admin", "DEVELOPER");

        mockMvc.perform(get("/v1/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void sprints_withValidJwt_shouldReturn200() throws Exception {
        String token = tokenProvider.generateToken("admin", "DEVELOPER");

        mockMvc.perform(get("/v1/sprints")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void tasks_withValidJwt_shouldReturn200() throws Exception {
        String token = tokenProvider.generateToken("admin", "DEVELOPER");

        mockMvc.perform(get("/v1/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void reports_withValidJwt_shouldReturn200() throws Exception {
        String token = tokenProvider.generateToken("admin", "DEVELOPER");

        mockMvc.perform(get("/v1/reports")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void calendar_withValidJwt_shouldReturn200() throws Exception {
        String token = tokenProvider.generateToken("admin", "DEVELOPER");

        mockMvc.perform(get("/v1/calendar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void prompts_withValidJwt_shouldReturn200() throws Exception {
        String token = tokenProvider.generateToken("admin", "DEVELOPER");

        mockMvc.perform(get("/v1/prompts/today")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    // --- Invalid JWT should be rejected ---

    @Test
    void sprints_withInvalidJwt_shouldReturn403() throws Exception {
        mockMvc.perform(get("/v1/sprints")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void tasks_withExpiredJwt_shouldReturn403() throws Exception {
        // Create a token with 1ms expiration that will be expired by the time we use it
        JwtTokenProvider shortLived = new JwtTokenProvider(
                "test-secret-key-for-unit-testing-minimum-32-bytes", 1L, 604800000L);
        String expiredToken = shortLived.generateToken("admin", "DEVELOPER");

        // Small delay to ensure expiration
        Thread.sleep(50);

        mockMvc.perform(get("/v1/tasks")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isForbidden());
    }

    // --- Auth /me endpoint ---

    @Test
    void authMe_withValidJwt_shouldReturn200() throws Exception {
        String token = tokenProvider.generateToken("admin", "DEVELOPER");

        mockMvc.perform(get("/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void authMe_withoutAuth_shouldReturn401() throws Exception {
        // /v1/auth/** is permitAll, but the controller itself returns 401
        // when authentication is null
        mockMvc.perform(get("/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
