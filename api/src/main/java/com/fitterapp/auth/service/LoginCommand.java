package com.fitterapp.auth.service;

import java.net.InetAddress;

public record LoginCommand(
        String email,
        String password,
        String userAgent,
        InetAddress ipAddress) {
}
