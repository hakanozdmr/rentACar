package hakan.rentacar.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitingConfig {

    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        return new ConcurrentHashMap<>();
    }

    public static Bucket createBucket() {
        // 100 request per minute
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    public static Bucket createAuthBucket() {
        // 5 login attempts per minute
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
}

