package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author anand
 */
@SpringBootApplication
public class Main {
    
    public static void main(String... args) throws InterruptedException {
        SpringApplication.run(Main.class, args);
        Thread.sleep(Long.MAX_VALUE);
    }
}
