package com.moveit.auth.feign;

import com.moveit.auth.dto.CreateUserRequest;
import com.moveit.auth.dto.UserCreatedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "${feign.user-service.url}")
public interface UserFeignClient {

    @PostMapping("/users")
    UserCreatedResponse createSpectator(@RequestBody CreateUserRequest request);

    @PostMapping("/users/admin")
    UserCreatedResponse createAdmin(@RequestBody CreateUserRequest request);
}