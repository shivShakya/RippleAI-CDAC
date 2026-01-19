package ai.ripple.ApiGateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ai.ripple.ApiGateway.security.JwtRoleFilter;

@Configuration
public class GatewayConfig {
   
	  private final JwtRoleFilter jwtRoleFilter;
	  
	  public GatewayConfig(JwtRoleFilter jwtRoleFilter) {
	        this.jwtRoleFilter = jwtRoleFilter;
	  }
	  
	  @Bean
	   public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
	        return builder.routes()
	                .route("ngo_service", r -> r.path("/ngo/**")
	                        .filters(f -> f.filter(jwtRoleFilter))
	                        .uri("lb://NGOSERVICE"))
	                .route("admin_service", r -> r.path("/admin/**")
	                        .filters(f -> f.filter(jwtRoleFilter))
	                        .uri("lb://ADMINSERVICE"))
	                .route("user_service", r -> r.path("/auth/**")
	                        .filters(f -> f.filter(jwtRoleFilter))
	                        .uri("lb://USERSERVICE"))
					.route("notification_service", r -> r.path("/notification/**")
							.filters(f -> f.filter(jwtRoleFilter))
							.uri("lb://NOTIFICATIONSERVICE"))

	                .build();
	    }
}
