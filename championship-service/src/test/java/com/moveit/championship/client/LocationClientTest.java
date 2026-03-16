package com.moveit.championship.client;

import com.moveit.championship.dto.LocationDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.assertj.core.api.Assertions.assertThat;

class LocationClientTest {

    @Test
    @DisplayName("Should declare LocationClient as a Feign client with expected name and url")
    void shouldDeclareFeignClientMetadata() {
        FeignClient annotation = LocationClient.class.getAnnotation(FeignClient.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.name()).isEqualTo("location-service");
        assertThat(annotation.url()).isEqualTo("${location.service.url:http://localhost:8084}");
    }

    @Test
    @DisplayName("Should expose getLocationById with GET /locations/{id}")
    void shouldExposeExpectedGetMapping() throws NoSuchMethodException {
        Method method = LocationClient.class.getMethod("getLocationById", Integer.class);
        GetMapping getMapping = method.getAnnotation(GetMapping.class);

        assertThat(getMapping).isNotNull();
        assertThat(getMapping.value()).containsExactly("/locations/{id}");
        assertThat(method.getReturnType()).isEqualTo(LocationDTO.class);
    }

    @Test
    @DisplayName("Should map id parameter as path variable")
    void shouldMapIdAsPathVariable() throws NoSuchMethodException {
        Method method = LocationClient.class.getMethod("getLocationById", Integer.class);
        Parameter parameter = method.getParameters()[0];
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);

        assertThat(pathVariable).isNotNull();
        assertThat(pathVariable.value()).isEqualTo("id");
        assertThat(parameter.getType()).isEqualTo(Integer.class);
    }
}
