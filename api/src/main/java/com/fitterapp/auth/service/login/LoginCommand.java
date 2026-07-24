package com.fitterapp.auth.service.login;

import java.net.InetAddress;

public record LoginCommand(
        String email,
        String password,
        String userAgent,
        InetAddress ipAddress) {
}
