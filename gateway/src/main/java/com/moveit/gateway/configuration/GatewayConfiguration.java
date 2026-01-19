package com.moveit.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class GatewayConfiguration {

    @Value("${AUTH_SERVICE_URL:http://localhost:8082}")
    private String authServiceUrl;

    @Value("${NOTIFICATION_SERVICE_URL:http://localhost:8088}")
    private String notificationServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return route("auth-service")
                .route(path("/auth/**"), http())
                .before(uri(authServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> notificationServiceRoute() {
        return route("notification-service")
                .route(path("/notifications/**"), http())
                .before(uri(notificationServiceUrl))
                .route(path("/subscriptions/**"), http())
                .before(uri(notificationServiceUrl))
                .route(path("/notification-types/**"), http())
                .before(uri(notificationServiceUrl))
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