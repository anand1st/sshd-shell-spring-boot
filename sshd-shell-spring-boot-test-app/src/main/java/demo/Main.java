package demo;

import javax.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.util.SocketUtils;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import redis.embedded.RedisServer;

/**
 *
 * @author anand
 */
@EnableCaching
@SpringBootApplication
public class Main extends WebMvcConfigurationSupport {

    private static final RedisServer REDIS_SERVER;

    static {
        int availablePort = SocketUtils.findAvailableTcpPort();
        System.setProperty("embeddedRedisPort", String.valueOf(availablePort));
        REDIS_SERVER = new RedisServer(availablePort);
        REDIS_SERVER.start();
    }

    public static void main(String... args) throws InterruptedException {
        SpringApplication.run(Main.class, args);
    }

    @PreDestroy
    void cleanUp() {
        REDIS_SERVER.stop();
    }

    @Override
    protected void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/test").setViewName("test");
    }
}
