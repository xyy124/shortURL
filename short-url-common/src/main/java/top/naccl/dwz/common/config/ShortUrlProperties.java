package top.naccl.dwz.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "short-url")
@Data
public class ShortUrlProperties {
    private String host = "http://localhost:8060";
    private int bloomExpectedInsertions = 1_000_000;
    private double bloomFalsePositiveRate = 0.01;
}
