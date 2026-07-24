package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.fitterapp.auth.repository.EmailVerificationTokenRepository;
import com.fitterapp.auth.service.register.RegisterCommand;
import com.fitterapp.auth.service.RegisterService;
import com.fitterapp.user.repository.UserRepository;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class RegistrationIntegrationTests {

    @Autowired
    private RegisterService registerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository verificationTokenRepository;

    @Test
    void persistsNewUserAndVerificationTokenThroughSpringData() {
        var result = registerService.register(new RegisterCommand(
                "Integration Test",
                "registration.integration@fitterapp.com",
                "+5544999999994",
                "StrongPassword123!"));

        assertThat(result.userId()).isNotNull();
        assertThat(userRepository.findByEmail("registration.integration@fitterapp.com")).isPresent();
        assertThat(verificationTokenRepository.count()).isEqualTo(1);
    }
}
