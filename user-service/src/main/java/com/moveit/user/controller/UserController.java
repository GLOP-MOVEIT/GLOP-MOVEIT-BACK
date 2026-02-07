package com.moveit.user.controller;

import com.moveit.user.dto.User;
import com.moveit.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User", description = "API de gestion des utilisateurs")
public class UserController {

    private final UserService userService;

    @GetMapping
    public Page<User> getAllUsers(@RequestBody Pageable pageable) {
        return this.userService.getAllUsers(pageable);
    }

    @GetMapping("/{id}")
    public User getUserProfile(@PathVariable Integer id) {
        return this.userService.getUserById(id);
    }

    @PutMapping
    public User updateUserProfile(@RequestBody User user) {
        return this.userService.updateUser(user);
    }

    @PostMapping
    public User createSpectator(@RequestBody User user) {
        return this.userService.createSpectator(user);
    }
}