package com.moveit.auth.service;

import com.moveit.auth.entity.Role;
import com.moveit.auth.entity.RoleEnum;
import com.moveit.auth.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleService(roleRepository);
    }

    @Test
    void findByName_ShouldReturnRole_WhenExists() {
        Role adminRole = new Role()
                .setId(1)
                .setName(RoleEnum.ADMIN)
                .setDescription("Administrator role");

        when(roleRepository.findByName(RoleEnum.ADMIN)).thenReturn(Optional.of(adminRole));

        Optional<Role> result = roleService.findByName(RoleEnum.ADMIN);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(RoleEnum.ADMIN);
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNotExists() {
        when(roleRepository.findByName(RoleEnum.ADMIN)).thenReturn(Optional.empty());

        Optional<Role> result = roleService.findByName(RoleEnum.ADMIN);

        assertThat(result).isEmpty();
    }

    @Test
    void init_ShouldCreateAllRoles_WhenNoneExist() {
        when(roleRepository.findByName(any(RoleEnum.class))).thenReturn(Optional.empty());

        roleService.init();

        verify(roleRepository, times(5)).save(any(Role.class));
    }

    @Test
    void init_ShouldNotCreateRoles_WhenAllExist() {
        Role existingRole = new Role().setId(1).setName(RoleEnum.SPECTATOR).setDescription("test");
        when(roleRepository.findByName(any(RoleEnum.class))).thenReturn(Optional.of(existingRole));

        roleService.init();

        verify(roleRepository, never()).save(any(Role.class));
    }
}
