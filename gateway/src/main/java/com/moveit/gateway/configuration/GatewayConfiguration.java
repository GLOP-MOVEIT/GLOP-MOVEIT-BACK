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

    @Value("${NOTIFICATION_SERVICE_URL:http://localhost:8088}")
    private String notificationServiceUrl;

    @Value("${CHAMPIONSHIP_SERVICE_URL:http://localhost:8083}")
    private String championshipServiceUrl;

    @Value("${LOCATION_SERVICE_URL:http://localhost:8084}")
    private String locationServiceUrl;

    @Value("${VOLUNTEER_SERVICE_URL:http://localhost:8085}")
    private String volunteerServiceUrl;

    @Value("${USER_SERVICE_URL:http://localhost:8086}")
    private String userServiceUrl;

    @Value("${RESULT_SERVICE_URL:http://localhost:8087")
    private String resultServiceUrl;

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

    @Bean
    public RouterFunction<ServerResponse> championshipServiceRoute() {
        return route("championship-service")
                .route(path("/championships/**"), http())
                .before(uri(championshipServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> trialServiceRoute() {
        return route("trial-service")
                .route(path("/trials/**"), http())
                .before(uri(championshipServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> locationServiceRoute() {
        return route("location-service")
                .route(path("/locations/**"), http())
                .before(uri(locationServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> volunteerServiceRoute() {
        return route("volunteer-service")
                .route(path("/volunteer/**"), http())
                .before(uri(volunteerServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return route("user-service")
                .route(path("/users/**"), http())
                .before(uri(userServiceUrl))
                .route(path("/roles/**"), http())
                .before(uri(userServiceUrl))
                .route(path("/requests/**"), http())
                .before(uri(userServiceUrl))
                .route(path("/tickets/**"), http())
                .before(uri(userServiceUrl))
                .route(path("/teams/**"), http())
                .before(uri(userServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> resultServiceRoute() {
        return route("result-service")
                .route(path("/results/**"), http())
                .before(uri(resultServiceUrl))
                .build();
    }
}