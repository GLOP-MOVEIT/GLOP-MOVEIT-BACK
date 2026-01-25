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

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return route("auth-service")
                .route(path("/auth/**"), http())
                .before(uri(authServiceUrl))
                .build();
    }

    // Exemple pour rajouter de nouvelles routes:
    // @Bean
    // public RouterFunction<ServerResponse> userServiceRoute() {
    //     return route("user-service")
    //             .route(path("/users/**"), http())
    //             .before(uri("http://localhost:8083"))
    //             .build();
    // }

    @Value("${LOCALIZATION_SERVICE_URL:http://localization-service:8085}")
    private String localizationServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> localizationServiceRoute() {
        return route("localization-service")
                .route(path("/api/location/**"), http())
                .before(uri(localizationServiceUrl))
                .build();
    }
}