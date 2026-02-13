package ao.gov.sgcd.pm.service;

import ao.gov.sgcd.pm.config.MonitorConfig;
import ao.gov.sgcd.pm.dto.MonitorDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.net.HttpURLConnection;
import java.net.URI;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorService {

    private final MonitorConfig monitorConfig;
    private final DataSource dataSource;

    private MonitorDTO cachedResult;
    private long cacheTimestamp = 0;

    public MonitorDTO getHealth() {
        long now = System.currentTimeMillis();
        if (cachedResult != null && (now - cacheTimestamp) < monitorConfig.getCacheTtlSeconds() * 1000L) {
            return cachedResult;
        }

        List<MonitorDTO.ServiceHealthDTO> results = new ArrayList<>();
        for (MonitorConfig.ServiceCheck service : monitorConfig.getServices()) {
            results.add(checkService(service));
        }

        // Overall status based on PM services
        List<MonitorDTO.ServiceHealthDTO> pmServices = results.stream()
                .filter(s -> "PM".equals(s.getGroup()))
                .toList();

        String overallStatus;
        if (pmServices.isEmpty()) {
            overallStatus = "UNKNOWN";
        } else {
            long pmUp = pmServices.stream().filter(s -> "UP".equals(s.getStatus())).count();
            if (pmUp == pmServices.size()) {
                overallStatus = "UP";
            } else if (pmUp == 0) {
                overallStatus = "DOWN";
            } else {
                overallStatus = "DEGRADED";
            }
        }

        cachedResult = MonitorDTO.builder()
                .overallStatus(overallStatus)
                .checkedAt(LocalDateTime.now())
                .services(results)
                .build();
        cacheTimestamp = now;

        return cachedResult;
    }

    private MonitorDTO.ServiceHealthDTO checkService(MonitorConfig.ServiceCheck service) {
        long start = System.currentTimeMillis();
        try {
            if ("MYSQL".equalsIgnoreCase(service.getType())) {
                return checkMysql(service, start);
            } else {
                return checkHttp(service, start);
            }
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.debug("Health check failed for {}: {}", service.getName(), e.getMessage());
            return MonitorDTO.ServiceHealthDTO.builder()
                    .name(service.getName())
                    .group(service.getGroup())
                    .type(service.getType())
                    .url(service.getUrl())
                    .status("DOWN")
                    .responseTimeMs(elapsed)
                    .lastChecked(LocalDateTime.now())
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    private MonitorDTO.ServiceHealthDTO checkHttp(MonitorConfig.ServiceCheck service, long start) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(service.getUrl()).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(monitorConfig.getTimeoutMs());
            conn.setReadTimeout(monitorConfig.getTimeoutMs());
            conn.setInstanceFollowRedirects(true);
            int code = conn.getResponseCode();
            long elapsed = System.currentTimeMillis() - start;
            conn.disconnect();

            String status = (code >= 200 && code < 400) ? "UP" : "DOWN";
            return MonitorDTO.ServiceHealthDTO.builder()
                    .name(service.getName())
                    .group(service.getGroup())
                    .type(service.getType())
                    .url(service.getUrl())
                    .status(status)
                    .responseTimeMs(elapsed)
                    .lastChecked(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            return MonitorDTO.ServiceHealthDTO.builder()
                    .name(service.getName())
                    .group(service.getGroup())
                    .type(service.getType())
                    .url(service.getUrl())
                    .status("DOWN")
                    .responseTimeMs(elapsed)
                    .lastChecked(LocalDateTime.now())
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    private MonitorDTO.ServiceHealthDTO checkMysql(MonitorConfig.ServiceCheck service, long start) {
        try (Connection conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(monitorConfig.getTimeoutMs() / 1000);
            long elapsed = System.currentTimeMillis() - start;
            return MonitorDTO.ServiceHealthDTO.builder()
                    .name(service.getName())
                    .group(service.getGroup())
                    .type(service.getType())
                    .url(service.getUrl())
                    .status(valid ? "UP" : "DOWN")
                    .responseTimeMs(elapsed)
                    .lastChecked(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            return MonitorDTO.ServiceHealthDTO.builder()
                    .name(service.getName())
                    .group(service.getGroup())
                    .type(service.getType())
                    .url(service.getUrl())
                    .status("DOWN")
                    .responseTimeMs(elapsed)
                    .lastChecked(LocalDateTime.now())
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
