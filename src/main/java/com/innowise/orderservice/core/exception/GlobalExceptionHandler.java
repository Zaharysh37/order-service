package com.innowise.orderservice.core.exception;

import com.innowise.orderservice.api.dto.GetErrorDto;
import jakarta.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import javax.naming.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

   private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

   @ExceptionHandler(EntityNotFoundException.class)
   public ResponseEntity<GetErrorDto> handleEntityNotFoundException(
       EntityNotFoundException ex, WebRequest request) {

      return buildResponseEntity(
          ex,
          ex.getMessage(),
          HttpStatus.NOT_FOUND,
          request
      );
   }

   @ExceptionHandler(DataIntegrityViolationException.class)
   public ResponseEntity<GetErrorDto> handleDataIntegrityViolation(
       DataIntegrityViolationException ex, WebRequest request) {

      String message = "Database conflict: " + ex.getMostSpecificCause().getMessage();

      logger.warn("Data integrity violation: {}", message);

      return buildResponseEntity(
          ex,
          "A resource with these details already exists or violates data constraints.",
          HttpStatus.CONFLICT,
          request
      );
   }

   @ExceptionHandler(MethodArgumentNotValidException.class)
   public ResponseEntity<GetErrorDto> handleValidationExceptions(
       MethodArgumentNotValidException ex, WebRequest request) {

      String errorMessage = ex.getBindingResult().getFieldErrors().stream()
          .map(error -> error.getField() + ": " + error.getDefaultMessage())
          .collect(Collectors.joining("; "));

      return buildResponseEntity(
          ex,
          errorMessage,
          HttpStatus.BAD_REQUEST,
          request
      );
   }

   @ExceptionHandler(HttpMessageNotReadableException.class)
   public ResponseEntity<GetErrorDto> handleMalformedJson(
       HttpMessageNotReadableException ex, WebRequest request) {

      return buildResponseEntity(
          ex,
          "Invalid request body: JSON is malformed.",
          HttpStatus.BAD_REQUEST,
          request
      );
   }

   @ExceptionHandler(AuthenticationException.class)
   public ResponseEntity<GetErrorDto> handleAuthenticationException(
       AuthenticationException ex, WebRequest request) {

      return buildResponseEntity(
          ex,
          "Authentication failed: " + ex.getMessage(),
          HttpStatus.UNAUTHORIZED,
          request
      );
   }

   @ExceptionHandler(AccessDeniedException.class)
   public ResponseEntity<GetErrorDto> handleAccessDeniedException(
       AccessDeniedException ex, WebRequest request) {

      return buildResponseEntity(
          ex,
          "Access Denied: You do not have permission to access this resource.",
          HttpStatus.FORBIDDEN,
          request
      );
   }

   @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
   public ResponseEntity<GetErrorDto> handleAuthenticationException(
       org.springframework.security.core.AuthenticationException ex, WebRequest request) {

      return buildResponseEntity(
          ex,
          "Authentication failed: " + ex.getMessage(),
          HttpStatus.UNAUTHORIZED,
          request
      );
   }

   @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
   public ResponseEntity<GetErrorDto> handleAccessDeniedException(
       org.springframework.security.access.AccessDeniedException ex, WebRequest request) {

      return buildResponseEntity(
          ex,
          "Access Denied: You do not have permission to access this resource.",
          HttpStatus.FORBIDDEN,
          request
      );
   }

   @ExceptionHandler(Exception.class)
   public ResponseEntity<GetErrorDto> handleGlobalException(
       Exception ex, WebRequest request) {

      logger.error("Unhandled exception caught: {}", ex.getMessage(), ex);

      return buildResponseEntity(
          ex,
          "An unexpected internal server error occurred.",
          HttpStatus.INTERNAL_SERVER_ERROR,
          request
      );
   }

   private ResponseEntity<GetErrorDto> buildResponseEntity(
       Exception ex, String message, HttpStatus status, WebRequest request) {

      GetErrorDto errorDto = new GetErrorDto(
          LocalDateTime.now(),
          status.value(),
          status.getReasonPhrase(),
          message,
          request.getDescription(false).replace("uri=", "")
      );

      return new ResponseEntity<>(errorDto, status);
   }
}
