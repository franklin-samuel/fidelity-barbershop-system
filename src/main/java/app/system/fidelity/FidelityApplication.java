package app.system.fidelity;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication
public class FidelityApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Fortaleza"));

        log.info("Timezone JVM: {}", TimeZone.getDefault().getID());
        log.info("Timezone Sistema: {}", System.getProperty("user.timezone"));
        log.info("Hora atual: {}", LocalDateTime.now());
    }

    public static void main(String[] args) {
        SpringApplication.run(FidelityApplication.class, args);
    }
}