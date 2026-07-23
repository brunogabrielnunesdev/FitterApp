package com.fitterapp.common.exception;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.fitterapp.auth.exception.AccountBlockedException;
import com.fitterapp.auth.exception.AccountNotPendingVerificationException;
import com.fitterapp.auth.exception.EmailAlreadyRegisteredException;
import com.fitterapp.auth.exception.EmailNotVerifiedException;
import com.fitterapp.auth.exception.InvalidCredentialsException;
import com.fitterapp.auth.exception.InvalidVerificationTokenException;
import com.fitterapp.auth.exception.RoleNotConfiguredException;
import com.fitterapp.auth.exception.VerificationTokenAlreadyUsedException;
import com.fitterapp.auth.exception.VerificationTokenExpiredException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ResponseEntity<ProblemDetail> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException exception) {
        return problem(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", exception.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException exception) {
        return problem(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", exception.getMessage());
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    ResponseEntity<ProblemDetail> handleEmailNotVerified(EmailNotVerifiedException exception) {
        return problem(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", exception.getMessage());
    }

    @ExceptionHandler(AccountBlockedException.class)
    ResponseEntity<ProblemDetail> handleAccountBlocked(AccountBlockedException exception) {
        return problem(HttpStatus.FORBIDDEN, "ACCOUNT_BLOCKED", exception.getMessage());
    }

    @ExceptionHandler(InvalidVerificationTokenException.class)
    ResponseEntity<ProblemDetail> handleInvalidToken(InvalidVerificationTokenException exception) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_VERIFICATION_TOKEN", exception.getMessage());
    }

    @ExceptionHandler(VerificationTokenExpiredException.class)
    ResponseEntity<ProblemDetail> handleExpiredToken(VerificationTokenExpiredException exception) {
        return problem(HttpStatus.GONE, "VERIFICATION_TOKEN_EXPIRED", exception.getMessage());
    }

    @ExceptionHandler({VerificationTokenAlreadyUsedException.class, AccountNotPendingVerificationException.class})
    ResponseEntity<ProblemDetail> handleVerificationConflict(RuntimeException exception) {
        return problem(HttpStatus.CONFLICT, "VERIFICATION_CONFLICT", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ProblemDetail detail = detail(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed");
        detail.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> handleUnreadableBody() {
        return problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Request body is malformed or missing");
    }

    @ExceptionHandler(RoleNotConfiguredException.class)
    ResponseEntity<ProblemDetail> handleConfigurationError(RoleNotConfiguredException exception) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_CONFIGURATION_ERROR", exception.getMessage());
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(detail(status, code, message));
    }

    private ProblemDetail detail(HttpStatus status, String code, String message) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(status.getReasonPhrase());
        detail.setProperty("code", code);
        detail.setProperty("timestamp", OffsetDateTime.now());
        return detail;
    }
}
