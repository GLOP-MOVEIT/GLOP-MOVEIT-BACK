package com.moveit.championship.client;

import com.moveit.championship.dto.UserResponseDTO;
import com.moveit.championship.dto.TeamResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user.service.url:http://localhost:8085}")
public interface UserClient {

    @GetMapping("/users/{id}")
    UserResponseDTO getUserById(@PathVariable("id") Integer id);

    @GetMapping("/teams/{id}")
    TeamResponseDTO getTeamById(@PathVariable("id") Integer id);
}


