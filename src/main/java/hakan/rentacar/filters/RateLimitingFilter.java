package hakan.rentacar.filters;

import hakan.rentacar.config.RateLimitingConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Autowired
    private Map<String, Bucket> rateLimitBuckets;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIpAddress(request);
        String endpoint = request.getRequestURI();
        
        // Determine bucket type based on endpoint
        Bucket bucket;
        if (endpoint.contains("/api/auth/")) {
            bucket = rateLimitBuckets.computeIfAbsent(clientIp + "_auth", 
                    k -> RateLimitingConfig.createAuthBucket());
        } else {
            bucket = rateLimitBuckets.computeIfAbsent(clientIp, 
                    k -> RateLimitingConfig.createBucket());
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            // Add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.addHeader("X-Rate-Limit-Reset", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
            
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            logger.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, endpoint);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\n" +
                    "  \"error\": \"Rate limit exceeded\",\n" +
                    "  \"message\": \"Too many requests. Please try again later.\",\n" +
                    "  \"retryAfter\": \"" + (probe.getNanosToWaitForRefill() / 1_000_000_000) + "\"\n" +
                    "}");
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
}

