package ao.gov.sgcd.pm.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitorDTO {

    private String overallStatus; // UP, DEGRADED, DOWN
    private LocalDateTime checkedAt;
    private List<ServiceHealthDTO> services;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ServiceHealthDTO {
        private String name;
        private String group;
        private String type;
        private String url;
        private String status; // UP, DOWN, UNKNOWN
        private Long responseTimeMs;
        private LocalDateTime lastChecked;
        private String errorMessage;
    }
}
