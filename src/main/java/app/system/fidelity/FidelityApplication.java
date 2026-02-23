package app.system.fidelity;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class FidelityApplication {

	public static void main(String[] args) {
		SpringApplication.run(FidelityApplication.class, args);
	}

}
