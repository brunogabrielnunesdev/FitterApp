package com.fitterapp.auth.exception;

import com.fitterapp.user.entity.RoleName;

public class RoleNotConfiguredException extends RuntimeException {

    public RoleNotConfiguredException(RoleName roleName) {
        super("Required role is not configured: " + roleName);
    }
}
