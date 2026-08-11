package com.fitterapp.common.exception;

import com.fitterapp.auth.exception.AccountBlockedException;
import com.fitterapp.auth.exception.AccountNotPendingVerificationException;
import com.fitterapp.auth.exception.EmailAlreadyRegisteredException;
import com.fitterapp.auth.exception.EmailNotVerifiedException;
import com.fitterapp.auth.exception.InvalidCredentialsException;
import com.fitterapp.auth.exception.InvalidPasswordResetTokenException;
import com.fitterapp.auth.exception.InvalidRefreshTokenException;
import com.fitterapp.auth.exception.InvalidVerificationTokenException;
import com.fitterapp.auth.exception.RoleNotConfiguredException;
import com.fitterapp.auth.exception.VerificationTokenAlreadyUsedException;
import com.fitterapp.auth.exception.VerificationTokenExpiredException;
import com.fitterapp.moderation.exception.ModerationReasonRequiredException;
import com.fitterapp.moderation.exception.ProfileModerationStateException;
import com.fitterapp.personal.exception.CrefAlreadyInUseException;
import com.fitterapp.personal.exception.DuplicateServiceAreaException;
import com.fitterapp.personal.exception.IncompleteProfileException;
import com.fitterapp.personal.exception.InvalidProfilePriceException;
import com.fitterapp.personal.exception.InvalidServiceAreaException;
import com.fitterapp.personal.exception.ProfileAlreadyExistsException;
import com.fitterapp.personal.exception.ProfileNotApprovedException;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.exception.ProfileNotPendingReviewException;
import com.fitterapp.personal.exception.ProfileRevisionNotEditableException;
import com.fitterapp.personal.exception.PublicProfileNotFoundException;
import com.fitterapp.personal.exception.ReviewReasonRequiredException;
import com.fitterapp.personal.exception.UnavailableModalityException;
import com.fitterapp.user.exception.UserNotFoundException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  ResponseEntity<ProblemDetail> handleUserNotFound(UserNotFoundException exception) {
    return problem(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", exception.getMessage());
  }

  @ExceptionHandler(ProfileNotFoundException.class)
  ResponseEntity<ProblemDetail> handleProfileNotFound(ProfileNotFoundException exception) {
    return problem(HttpStatus.NOT_FOUND, "PROFILE_NOT_FOUND", exception.getMessage());
  }

  @ExceptionHandler(PublicProfileNotFoundException.class)
  ResponseEntity<ProblemDetail> handlePublicProfileNotFound(
      PublicProfileNotFoundException exception) {
    return problem(HttpStatus.NOT_FOUND, "PUBLIC_PROFILE_NOT_FOUND", exception.getMessage());
  }

  @ExceptionHandler({ProfileAlreadyExistsException.class, CrefAlreadyInUseException.class})
  ResponseEntity<ProblemDetail> handleProfileConflict(RuntimeException exception) {
    return problem(HttpStatus.CONFLICT, "PROFILE_CONFLICT", exception.getMessage());
  }

  @ExceptionHandler({
    IncompleteProfileException.class,
    InvalidProfilePriceException.class,
    InvalidServiceAreaException.class,
    DuplicateServiceAreaException.class,
    ModerationReasonRequiredException.class,
    ReviewReasonRequiredException.class,
    UnavailableModalityException.class
  })
  ResponseEntity<ProblemDetail> handleProfileValidation(RuntimeException exception) {
    return problem(HttpStatus.BAD_REQUEST, "PROFILE_VALIDATION_ERROR", exception.getMessage());
  }

  @ExceptionHandler({
    ProfileRevisionNotEditableException.class,
    ProfileNotPendingReviewException.class,
    ProfileNotApprovedException.class
  })
  ResponseEntity<ProblemDetail> handleProfileState(RuntimeException exception) {
    return problem(HttpStatus.CONFLICT, "PROFILE_INVALID_STATE", exception.getMessage());
  }

  @ExceptionHandler(ProfileModerationStateException.class)
  ResponseEntity<ProblemDetail> handleProfileModerationState(
      ProfileModerationStateException exception) {
    return problem(HttpStatus.CONFLICT, "PROFILE_MODERATION_INVALID_STATE", exception.getMessage());
  }

  @ExceptionHandler(EmailAlreadyRegisteredException.class)
  ResponseEntity<ProblemDetail> handleEmailAlreadyRegistered(
      EmailAlreadyRegisteredException exception) {
    return problem(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", exception.getMessage());
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException exception) {
    return problem(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", exception.getMessage());
  }

  @ExceptionHandler(InvalidRefreshTokenException.class)
  ResponseEntity<ProblemDetail> handleInvalidRefreshToken(InvalidRefreshTokenException exception) {
    return problem(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", exception.getMessage());
  }

  @ExceptionHandler(InvalidPasswordResetTokenException.class)
  ResponseEntity<ProblemDetail> handleInvalidPasswordResetToken(
      InvalidPasswordResetTokenException exception) {
    return problem(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD_RESET_TOKEN", exception.getMessage());
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

  @ExceptionHandler({
    VerificationTokenAlreadyUsedException.class,
    AccountNotPendingVerificationException.class
  })
  ResponseEntity<ProblemDetail> handleVerificationConflict(RuntimeException exception) {
    return problem(HttpStatus.CONFLICT, "VERIFICATION_CONFLICT", exception.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception) {
    Map<String, String> errors = new LinkedHashMap<>();
    exception
        .getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
    ProblemDetail detail =
        detail(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed");
    detail.setProperty("errors", errors);
    return ResponseEntity.badRequest().body(detail);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<ProblemDetail> handleUnreadableBody() {
    return problem(
        HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Request body is malformed or missing");
  }

  @ExceptionHandler(RoleNotConfiguredException.class)
  ResponseEntity<ProblemDetail> handleConfigurationError(RoleNotConfiguredException exception) {
    return problem(
        HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_CONFIGURATION_ERROR", exception.getMessage());
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
