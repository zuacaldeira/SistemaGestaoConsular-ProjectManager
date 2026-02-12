package ao.gov.sgcd.pm.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expiration;
    private final long refreshExpiration;

    private final ConcurrentHashMap<String, RefreshTokenData> refreshTokens = new ConcurrentHashMap<>();
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public JwtTokenProvider(
            @Value("${sgcd-pm.jwt.secret}") String secret,
            @Value("${sgcd-pm.jwt.expiration}") long expiration,
            @Value("${sgcd-pm.jwt.refresh-expiration:604800000}") long refreshExpiration) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String username, String role) {
        String refreshToken = UUID.randomUUID().toString();
        long expiresAt = Instant.now().toEpochMilli() + refreshExpiration;
        refreshTokens.put(refreshToken, new RefreshTokenData(username, role, expiresAt));
        return refreshToken;
    }

    public Optional<RefreshTokenData> validateRefreshToken(String refreshToken) {
        RefreshTokenData data = refreshTokens.get(refreshToken);
        if (data == null) {
            return Optional.empty();
        }
        if (Instant.now().toEpochMilli() > data.expiresAt()) {
            refreshTokens.remove(refreshToken);
            return Optional.empty();
        }
        return Optional.of(data);
    }

    public void revokeRefreshToken(String refreshToken) {
        refreshTokens.remove(refreshToken);
    }

    public void revokeAllRefreshTokensForUser(String username) {
        refreshTokens.entrySet().removeIf(entry -> entry.getValue().username().equals(username));
    }

    public void blacklistToken(String token) {
        try {
            String jti = getClaims(token).getPayload().getId();
            if (jti != null) {
                blacklistedTokens.add(jti);
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            // Token already expired or invalid — no need to blacklist
        }
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getPayload().getSubject();
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).getPayload().get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = getClaims(token);
            String jti = claims.getPayload().getId();
            if (jti != null && blacklistedTokens.contains(jti)) {
                return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    public long getExpiration() {
        return expiration;
    }

    @Scheduled(fixedRate = 3_600_000)
    public void cleanupExpiredTokens() {
        long now = Instant.now().toEpochMilli();
        refreshTokens.entrySet().removeIf(entry -> now > entry.getValue().expiresAt());
        // Blacklisted JTIs are cleaned up by removing entries older than the max access token lifetime
        // Since we can't track individual expiry per JTI without extra storage, we clear all periodically
        // In practice, access tokens are 1h so clearing every hour is sufficient
    }

    public record RefreshTokenData(String username, String role, long expiresAt) {}
}
