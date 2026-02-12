package ao.gov.sgcd.pm.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000; // 1 minute
    private static final long LOCKOUT_MS = 300_000; // 5 minutes

    private final ConcurrentHashMap<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public boolean isAllowed(String ipAddress) {
        LoginAttempt attempt = attempts.get(ipAddress);
        if (attempt == null) {
            return true;
        }
        long now = Instant.now().toEpochMilli();
        if (attempt.lockedUntil > 0 && now < attempt.lockedUntil) {
            return false;
        }
        if (attempt.lockedUntil > 0 && now >= attempt.lockedUntil) {
            attempts.remove(ipAddress);
            return true;
        }
        if (now - attempt.firstAttemptTime > WINDOW_MS) {
            attempts.remove(ipAddress);
            return true;
        }
        return attempt.count < MAX_ATTEMPTS;
    }

    public void recordFailedAttempt(String ipAddress) {
        long now = Instant.now().toEpochMilli();
        attempts.compute(ipAddress, (key, existing) -> {
            if (existing == null || now - existing.firstAttemptTime > WINDOW_MS) {
                return new LoginAttempt(1, now, 0);
            }
            int newCount = existing.count + 1;
            long lockUntil = newCount >= MAX_ATTEMPTS ? now + LOCKOUT_MS : 0;
            return new LoginAttempt(newCount, existing.firstAttemptTime, lockUntil);
        });
    }

    public void resetAttempts(String ipAddress) {
        attempts.remove(ipAddress);
    }

    @Scheduled(fixedRate = 60_000)
    public void cleanupExpiredEntries() {
        long now = Instant.now().toEpochMilli();
        attempts.entrySet().removeIf(entry -> {
            LoginAttempt a = entry.getValue();
            if (a.lockedUntil > 0) {
                return now >= a.lockedUntil;
            }
            return now - a.firstAttemptTime > WINDOW_MS;
        });
    }

    private static class LoginAttempt {
        final int count;
        final long firstAttemptTime;
        final long lockedUntil;

        LoginAttempt(int count, long firstAttemptTime, long lockedUntil) {
            this.count = count;
            this.firstAttemptTime = firstAttemptTime;
            this.lockedUntil = lockedUntil;
        }
    }
}
