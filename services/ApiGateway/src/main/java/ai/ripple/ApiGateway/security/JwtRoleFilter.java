package ai.ripple.ApiGateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.security.Key;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtRoleFilter implements GatewayFilter {

    @Value("${jwt.SECRET}")
    private String secret; 

    private Key signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        System.out.println("[Gateway] Incoming request path: " + path);

        // Allow register/login without JWT
        if (path.startsWith("/auth/register") || path.startsWith("/auth/login") || path.startsWith("/notification/")) {
            System.out.println("[Gateway] Skipping JWT check for path: " + path);
            return chain.filter(exchange);
        }

        // Extract Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        System.out.println("[Gateway] Authorization header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[Gateway] Missing or invalid Authorization header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7).trim();

        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            System.out.println("[Gateway] JWT parsed successfully, subject: " + claims.getSubject());
        } catch (Exception e) {
            System.out.println("[Gateway] Invalid JWT: " + e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Extract roles safely
        List<String> roles = new ArrayList<>();
        Object rolesObj = claims.get("role"); 
        if (rolesObj instanceof String) {
            roles.add((String) rolesObj);
        } else if (rolesObj instanceof List<?>) {
            for (Object r : (List<?>) rolesObj) {
                roles.add(String.valueOf(r));
            }
        }
        System.out.println("[Gateway] Roles extracted: " + roles);

        // Check if user role is allowed for the requested path
        boolean allowed = roles.stream().anyMatch(role -> {
            String r = role.toUpperCase();
            return (r.equals("NGO") && path.startsWith("/ngo")) ||
                   (r.equals("USER") && path.startsWith("/auth/user")) ||
                   (r.equals("NGO") && path.startsWith("/auth/ngo")) ||
                   (r.equals("USER") && path.startsWith("/user")) ||
                   (r.equals("ADMIN") && path.startsWith("/admin"));
        });

        if (!allowed) {
            System.out.println("[Gateway] Access forbidden for roles: " + roles + " on path: " + path);
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // Mutate request headers correctly
        org.springframework.http.server.reactive.ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Email", claims.getSubject())
                .header("X-User-Roles", String.join(",", roles))
                .build();

        System.out.println("[Gateway] Mutated headers: " + mutatedRequest.getHeaders());

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // Forward mutated request
        return chain.filter(mutatedExchange);
    }
}
