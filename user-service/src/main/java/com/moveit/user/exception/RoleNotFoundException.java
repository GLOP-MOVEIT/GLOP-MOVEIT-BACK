package com.moveit.user.exception;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(String roleName) {
        super("Role not found with name: " + roleName);
    }
}