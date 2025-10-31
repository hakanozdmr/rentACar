package hakan.rentacar.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // Skip JWT validation for Swagger UI and public endpoints
        if (requestURI.startsWith("/swagger-ui") || 
            requestURI.startsWith("/v3/api-docs") || 
            requestURI.startsWith("/api/auth") ||
            requestURI.startsWith("/api/test")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String jwt = parseJwt(request);
            logger.info("Request {} - JWT token present: {}", requestURI, jwt != null ? "YES" : "NO");
            
            if (jwt != null) {
                logger.info("JWT token: {}...", jwt.substring(0, Math.min(20, jwt.length())));
                String username = jwtUtils.extractUsername(jwt);
                logger.info("Extracted username: {}", username);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtUtils.validateToken(jwt, userDetails)) {
                    logger.info("Token validation successful for user: {}", username);
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    logger.warn("Token validation failed for user: {}", username);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        logger.info("Authorization header: {}", headerAuth != null ? headerAuth.substring(0, Math.min(50, headerAuth.length())) + "..." : "NULL");
        
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            logger.info("Extracted JWT token: {}...", token.substring(0, Math.min(20, token.length())));
            return token;
        }
        
        logger.warn("No valid Authorization header found");
        return null;
    }
}
