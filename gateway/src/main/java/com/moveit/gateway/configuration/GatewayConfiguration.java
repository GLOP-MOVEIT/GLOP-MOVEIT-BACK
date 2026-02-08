package com.moveit.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class GatewayConfiguration {

    @Value("${AUTH_SERVICE_URL:http://localhost:8082}")
    private String authServiceUrl;

    @Value("${CHAMPIONSHIP_SERVICE_URL:http://localhost:8083}")
    private String championshipServiceUrl;

    @Value("${USER_SERVICE_URL:http://localhost:8086}")
    private String userServiceUrl;

    @Value("${ROLE_SERVICE_URL:http://localhost:8086}")
    private String roleServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return route("auth-service")
                .route(path("/auth/**"), http())
                .before(uri(authServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> championshipServiceRoute() {
        return route("championship-service")
                .route(path("/championships/**"), http())
                .before(uri(championshipServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return route("user-service")
                .route(path("/users/**"), http())
                .before(uri(userServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> roleServiceRoute() {
        return route("role-service")
                .route(path("/roles/**"), http())
                .before(uri(roleServiceUrl))
                .build();
    }
}