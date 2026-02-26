package com.moveit.championship.client;

import com.moveit.championship.dto.LocationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class LocationClient {

    private final RestTemplate restTemplate;
    private final String locationServiceUrl;

    public LocationClient(RestTemplate restTemplate,
                          @Value("${location.service.url:http://localhost:8084}") String locationServiceUrl) {
        this.restTemplate = restTemplate;
        this.locationServiceUrl = locationServiceUrl;
    }

    public LocationDTO getLocationById(Integer id) {
        try {
            String url = locationServiceUrl + "/locations/" + id;
            return restTemplate.getForObject(url, LocationDTO.class);
        } catch (Exception e) {
            log.error("Error fetching location with id: {}", id, e);
            return null;
        }
    }
}
