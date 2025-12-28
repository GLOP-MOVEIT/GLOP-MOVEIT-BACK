package com.moveit.gateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

/**
 * Configuration des routes du Gateway.
 * Définit le routage des requêtes vers les différents microservices.
 */
@Configuration
public class GatewayConfiguration {

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return route("auth-service")
                .route(path("/auth/**"), http())
                .before(uri("http://localhost:8082"))
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
}
