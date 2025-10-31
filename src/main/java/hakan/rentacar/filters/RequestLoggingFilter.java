package hakan.rentacar.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // Generate unique request ID
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        MDC.put("clientIp", getClientIpAddress(request));
        
        long startTime = System.currentTimeMillis();
        
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        
        try {
            // Log incoming request
            logger.info("INCOMING REQUEST - Method: {}, URI: {}, Headers: {}, User-Agent: {}", 
                    request.getMethod(), 
                    request.getRequestURI(),
                    request.getHeaderNames(),
                    request.getHeader("User-Agent"));
            
            filterChain.doFilter(wrappedRequest, wrappedResponse);
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log outgoing response
            logger.info("OUTGOING RESPONSE - Status: {}, Duration: {}ms, Size: {} bytes", 
                    response.getStatus(), 
                    duration,
                    wrappedResponse.getContentSize());
            
            // Copy response body back to original response
            wrappedResponse.copyBodyToResponse();
            
            // Clear MDC
            MDC.clear();
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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip logging for actuator endpoints and static resources
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator/") || 
               uri.startsWith("/static/") || 
               uri.contains(".");
    }
}

