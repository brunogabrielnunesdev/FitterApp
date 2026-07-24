package com.fitterapp.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fitterapp.auth.exception.InvalidCredentialsException;
import com.fitterapp.auth.mapper.AuthMapper;
import com.fitterapp.auth.service.ConfirmEmailService;
import com.fitterapp.auth.service.LoginService;
import com.fitterapp.auth.service.RegisterService;
import com.fitterapp.auth.service.ResendConfirmationService;
import com.fitterapp.auth.service.login.LoginCommand;
import com.fitterapp.auth.service.login.LoginResult;
import com.fitterapp.auth.service.register.RegisterCommand;
import com.fitterapp.auth.service.register.RegisterResult;
import com.fitterapp.common.exception.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
class AuthControllerTests {

    @Mock private RegisterService registerService;
    @Mock private LoginService loginService;
    @Mock private ConfirmEmailService confirmEmailService;
    @Mock private ResendConfirmationService resendConfirmationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(
                registerService,
                loginService,
                confirmEmailService,
                resendConfirmationService,
                new AuthMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registersUserAndReturnsCreated() throws Exception {
        UUID userId = UUID.randomUUID();
        when(registerService.register(any())).thenReturn(new RegisterResult(userId));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Bruno Gabriel",
                                  "email": "bruno@fitterapp.com",
                                  "phoneNumber": "+5544999999999",
                                  "password": "StrongPassword123!"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()));

        verify(registerService).register(new RegisterCommand(
                "Bruno Gabriel", "bruno@fitterapp.com", "+5544999999999", "StrongPassword123!"));
    }

    @Test
    void logsInAndReturnsBearerTokens() throws Exception {
        when(loginService.login(any())).thenReturn(new LoginResult("access", "refresh", 900));

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("User-Agent", "FitterApp/1.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bruno@fitterapp.com","password":"StrongPassword123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"))
                .andExpect(jsonPath("$.expiresInSeconds").value(900));

        ArgumentCaptor<LoginCommand> commandCaptor = ArgumentCaptor.forClass(LoginCommand.class);
        verify(loginService).login(commandCaptor.capture());
        assertThat(commandCaptor.getValue().userAgent()).isEqualTo("FitterApp/1.0");
        assertThat(commandCaptor.getValue().ipAddress()).isNotNull();
    }

    @Test
    void returnsStandardProblemForInvalidCredentials() throws Exception {
        when(loginService.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bruno@fitterapp.com","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    void rejectsInvalidRegisterFieldsWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"","email":"invalid","phoneNumber":"449999","password":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.phoneNumber").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void confirmsEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/email/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"raw-token\"}"))
                .andExpect(status().isNoContent());

        verify(confirmEmailService).confirm("raw-token");
    }

    @Test
    void acceptsConfirmationResendWithNeutralResponse() throws Exception {
        mockMvc.perform(post("/api/v1/auth/email/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bruno@fitterapp.com\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$").doesNotExist());

        verify(resendConfirmationService).resend("bruno@fitterapp.com");
    }
}
