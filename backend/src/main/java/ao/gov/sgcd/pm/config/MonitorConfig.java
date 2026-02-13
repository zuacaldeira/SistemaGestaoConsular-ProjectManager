package ao.gov.sgcd.pm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "sgcd-pm.monitor")
@Getter
@Setter
public class MonitorConfig {

    private int cacheTtlSeconds = 30;
    private int timeoutMs = 5000;
    private List<ServiceCheck> services = new ArrayList<>();

    @Getter
    @Setter
    public static class ServiceCheck {
        private String name;
        private String url;
        private String type; // HTTP, TCP, MYSQL
        private String group; // PM, MVP
    }
}
