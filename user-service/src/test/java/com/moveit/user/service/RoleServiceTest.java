package com.moveit.user.service;

import com.moveit.user.dto.Role;
import com.moveit.user.entity.RoleEntity;
import com.moveit.user.exception.RoleNotFoundException;
import com.moveit.user.mapper.RoleMapper;
import com.moveit.user.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleService roleService;

    private List<RoleEntity> testRoleEntities;
    private List<Role> testRoles;

    @BeforeEach
    void setUp() {
        testRoleEntities = List.of(
                createRoleEntity("SPECTATOR"),
                createRoleEntity("VOLUNTEER"),
                createRoleEntity("ATHLETE"),
                createRoleEntity("ADMIN"),
                createRoleEntity("REFEREE")
        );

        testRoles = List.of(
                new Role("SPECTATOR"),
                new Role("VOLUNTEER"),
                new Role("ATHLETE"),
                new Role("ADMIN"),
                new Role("REFEREE")
        );
    }

    private RoleEntity createRoleEntity(String name) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName(name);
        return roleEntity;
    }

    @Test
    void getAllRole_ShouldReturnListOfRoles() {
        when(roleRepository.findAll()).thenReturn(testRoleEntities);
        when(roleMapper.toDtoList(testRoleEntities)).thenReturn(testRoles);

        List<Role> result = roleService.getAllRole();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(5);
        assertThat(result.get(0).getName()).isEqualTo("SPECTATOR");
        assertThat(result.get(1).getName()).isEqualTo("VOLUNTEER");
        assertThat(result.get(2).getName()).isEqualTo("ATHLETE");
        assertThat(result.get(3).getName()).isEqualTo("ADMIN");
        assertThat(result.get(4).getName()).isEqualTo("REFEREE");

        verify(roleRepository).findAll();
        verify(roleMapper).toDtoList(testRoleEntities);
    }

    @Test
    void getAllRole_ShouldReturnEmptyList_WhenNoRoles() {
        when(roleRepository.findAll()).thenReturn(List.of());
        when(roleMapper.toDtoList(List.of())).thenReturn(List.of());

        List<Role> result = roleService.getAllRole();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(roleRepository).findAll();
        verify(roleMapper).toDtoList(List.of());
    }

    @Test
    void getRoleEntityByName_ShouldReturnRoleEntity_WhenRoleExists() {
        RoleEntity spectatorRole = createRoleEntity("SPECTATOR");
        when(roleRepository.findByName("SPECTATOR")).thenReturn(Optional.of(spectatorRole));

        RoleEntity result = roleService.getRoleEntityByName("SPECTATOR");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("SPECTATOR");

        verify(roleRepository).findByName("SPECTATOR");
    }

    @Test
    void getRoleEntityByName_ShouldThrowRoleNotFoundException_WhenRoleNotFound() {
        when(roleRepository.findByName("INVALID_ROLE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getRoleEntityByName("INVALID_ROLE"))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found with name: INVALID_ROLE");

        verify(roleRepository).findByName("INVALID_ROLE");
    }

    @Test
    void getRoleEntityByName_ShouldReturnCorrectRole_ForEachRoleType() {
        // Test for VOLUNTEER
        RoleEntity volunteerRole = createRoleEntity("VOLUNTEER");
        when(roleRepository.findByName("VOLUNTEER")).thenReturn(Optional.of(volunteerRole));

        RoleEntity result1 = roleService.getRoleEntityByName("VOLUNTEER");
        assertThat(result1.getName()).isEqualTo("VOLUNTEER");

        // Test for ATHLETE
        RoleEntity athleteRole = createRoleEntity("ATHLETE");
        when(roleRepository.findByName("ATHLETE")).thenReturn(Optional.of(athleteRole));

        RoleEntity result2 = roleService.getRoleEntityByName("ATHLETE");
        assertThat(result2.getName()).isEqualTo("ATHLETE");

        // Test for ADMIN
        RoleEntity adminRole = createRoleEntity("ADMIN");
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));

        RoleEntity result3 = roleService.getRoleEntityByName("ADMIN");
        assertThat(result3.getName()).isEqualTo("ADMIN");

        // Test for REFEREE
        RoleEntity refereeRole = createRoleEntity("REFEREE");
        when(roleRepository.findByName("REFEREE")).thenReturn(Optional.of(refereeRole));

        RoleEntity result4 = roleService.getRoleEntityByName("REFEREE");
        assertThat(result4.getName()).isEqualTo("REFEREE");

        verify(roleRepository).findByName("VOLUNTEER");
        verify(roleRepository).findByName("ATHLETE");
        verify(roleRepository).findByName("ADMIN");
        verify(roleRepository).findByName("REFEREE");
    }
}

