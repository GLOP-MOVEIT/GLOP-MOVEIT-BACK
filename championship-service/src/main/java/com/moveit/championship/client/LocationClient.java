package com.moveit.championship.client;

import com.moveit.championship.dto.LocationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "location-service", url = "${location.service.url:http://localhost:8084}")
public interface LocationClient {

    @GetMapping("/locations/{id}")
    LocationDTO getLocationById(@PathVariable("id") Integer id);
}
