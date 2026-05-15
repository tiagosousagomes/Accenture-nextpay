package acc.br.nextpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NextPayApplication {

	public static void main(String[] args) {
		SpringApplication.run(NextPayApplication.class, args);
	}

}
