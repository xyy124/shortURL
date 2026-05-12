package top.naccl.dwz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import top.naccl.dwz.common.config.ShortUrlProperties;

@SpringBootApplication(scanBasePackages = "top.naccl.dwz")
@EnableConfigurationProperties(ShortUrlProperties.class)
public class ShortUrlApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShortUrlApplication.class, args);
    }
}
