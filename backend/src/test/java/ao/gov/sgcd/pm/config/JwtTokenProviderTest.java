package ao.gov.sgcd.pm.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-for-unit-testing-minimum-32-bytes";
    private static final long EXPIRATION = 86400000L;

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, EXPIRATION, 604800000L);
    }

    @Test
    void generateToken_shouldProduceValidJwt() {
        String token = tokenProvider.generateToken("admin", "DEVELOPER");

        assertNotNull(token);
        assertFalse(token.isBlank());
        // JWT has three parts separated by dots
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        String token = tokenProvider.generateToken("admin", "DEVELOPER");

        String username = tokenProvider.getUsernameFromToken(token);

        assertEquals("admin", username);
    }

    @Test
    void getUsernameFromToken_shouldReturnStakeholderUsername() {
        String token = tokenProvider.generateToken("stakeholder", "STAKEHOLDER");

        String username = tokenProvider.getUsernameFromToken(token);

        assertEquals("stakeholder", username);
    }

    @Test
    void getRoleFromToken_shouldReturnDeveloperRole() {
        String token = tokenProvider.generateToken("admin", "DEVELOPER");

        String role = tokenProvider.getRoleFromToken(token);

        assertEquals("DEVELOPER", role);
    }

    @Test
    void getRoleFromToken_shouldReturnStakeholderRole() {
        String token = tokenProvider.generateToken("stakeholder", "STAKEHOLDER");

        String role = tokenProvider.getRoleFromToken(token);

        assertEquals("STAKEHOLDER", role);
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = tokenProvider.generateToken("admin", "DEVELOPER");

        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForTamperedToken() {
        String token = tokenProvider.generateToken("admin", "DEVELOPER");
        String tampered = token + "tampered";

        assertFalse(tokenProvider.validateToken(tampered));
    }

    @Test
    void validateToken_shouldReturnFalseForNullToken() {
        assertFalse(tokenProvider.validateToken(null));
    }

    @Test
    void validateToken_shouldReturnFalseForEmptyToken() {
        assertFalse(tokenProvider.validateToken(""));
    }

    @Test
    void validateToken_shouldReturnFalseForRandomString() {
        assertFalse(tokenProvider.validateToken("not-a-jwt-token"));
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() throws InterruptedException {
        // Create provider with very short expiration (1 ms)
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(SECRET, 1L, 604800000L);
        String token = shortLivedProvider.generateToken("admin", "DEVELOPER");

        // Wait for token to expire
        Thread.sleep(50);

        assertFalse(shortLivedProvider.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForTokenSignedWithDifferentKey() {
        JwtTokenProvider otherProvider = new JwtTokenProvider(
                "a-completely-different-secret-key-32-bytes", EXPIRATION, 604800000L);
        String token = otherProvider.generateToken("admin", "DEVELOPER");

        assertFalse(tokenProvider.validateToken(token));
    }

    @Test
    void getExpiration_shouldReturnConfiguredExpiration() {
        assertEquals(EXPIRATION, tokenProvider.getExpiration());
    }

    @Test
    void getExpiration_shouldReturnCustomExpiration() {
        JwtTokenProvider customProvider = new JwtTokenProvider(SECRET, 3600000L, 604800000L);

        assertEquals(3600000L, customProvider.getExpiration());
    }

    @Test
    void constructor_shouldPadShortKey() {
        // Key shorter than 32 bytes should be padded
        JwtTokenProvider shortKeyProvider = new JwtTokenProvider("short", EXPIRATION, 604800000L);

        String token = shortKeyProvider.generateToken("admin", "DEVELOPER");
        assertNotNull(token);
        assertTrue(shortKeyProvider.validateToken(token));
        assertEquals("admin", shortKeyProvider.getUsernameFromToken(token));
    }

    @Test
    void generateToken_shouldProduceDifferentTokensForDifferentUsers() {
        String token1 = tokenProvider.generateToken("admin", "DEVELOPER");
        String token2 = tokenProvider.generateToken("stakeholder", "STAKEHOLDER");

        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_shouldProduceDifferentTokensForDifferentRoles() {
        String token1 = tokenProvider.generateToken("user", "DEVELOPER");
        String token2 = tokenProvider.generateToken("user", "STAKEHOLDER");

        assertNotEquals(token1, token2);
    }

    @Test
    void roundTrip_shouldPreserveUsernameAndRole() {
        String username = "testuser";
        String role = "DEVELOPER";

        String token = tokenProvider.generateToken(username, role);

        assertEquals(username, tokenProvider.getUsernameFromToken(token));
        assertEquals(role, tokenProvider.getRoleFromToken(token));
        assertTrue(tokenProvider.validateToken(token));
    }
}
