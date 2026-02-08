package com.moveit.user.config;

import com.moveit.user.entity.RoleEntity;
import com.moveit.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    private static final List<String> DEFAULT_ROLES = Arrays.asList(
            "SPECTATOR",
            "VOLUNTEER",
            "ATHLETE",
            "ADMIN",
            "REFEREE"
    );

    @Override
    public void run(String... args) {
        DEFAULT_ROLES.forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                RoleEntity role = new RoleEntity();
                role.setName(roleName);
                roleRepository.save(role);
            }
        });
    }
}