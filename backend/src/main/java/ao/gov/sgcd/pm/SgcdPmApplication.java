package ao.gov.sgcd.pm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SgcdPmApplication {

    public static void main(String[] args) {
        SpringApplication.run(SgcdPmApplication.class, args);
    }
}
