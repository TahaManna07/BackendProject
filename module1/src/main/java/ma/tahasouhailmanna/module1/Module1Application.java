package ma.tahasouhailmanna.module1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@Slf4j
public class Module1Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Module1Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                log.info("taha hash: {}", encoder.encode("taha258"));
                log.info("souhail hash: {}", encoder.encode("souhail258"));
            }
        }



