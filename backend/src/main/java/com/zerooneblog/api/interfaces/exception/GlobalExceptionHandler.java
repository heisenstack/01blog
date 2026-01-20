package com.zerooneblog.api.interfaces.exception;

import com.zerooneblog.api.interfaces.dto.MessageResponse;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// Global exception handler for unified error responses across the API
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle validation errors from request body validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String detailedMessage = errors.entrySet().stream()
                .map(e -> String.format("%s: %s", e.getKey(), e.getValue()))
                .collect(Collectors.joining("; "));

        MessageResponse errorResponse = new MessageResponse(
                "FAILURE",
                detailedMessage);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle illegal argument exceptions
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {

        MessageResponse errorResponse = new MessageResponse("FAILURE", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle notification not found exceptions
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<MessageResponse> handleNotificationNotFound(
            NotificationNotFoundException ex,
            WebRequest request) {

        MessageResponse errorResponse = new MessageResponse("FAILURE", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Handle authorization denied exceptions
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<MessageResponse> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        MessageResponse errorResponse = new MessageResponse(
                "FAILURE",
                "You don't have permission to access this resource.");
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // Handle file upload size exceeded exceptions
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<MessageResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        MessageResponse errorResponse = new MessageResponse(
                "FAILURE",
                "The file or content size is too large.");

        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    // Handle authentication exceptions (invalid username/password)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<MessageResponse> handleAuthenticationException(AuthenticationException ex) {

        MessageResponse errorResponse = new MessageResponse(
                "FAILURE",
                "Invalid username or password.");

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // Handle unauthorized action exceptions
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<MessageResponse> handleUnauthorizedAction(
            UnauthorizedActionException ex,
            WebRequest request) {

        MessageResponse errorResponse = new MessageResponse("FAILURE", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // Handle resource not found exceptions
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MessageResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {

        MessageResponse errorResponse = new MessageResponse("FAILURE", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Handle type mismatch in request parameters
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<MessageResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String requiredType = (ex.getRequiredType() != null)
                ? ex.getRequiredType().getSimpleName()
                : "required type";

        String detailMessage = String.format(
                "The parameter '%s' should be of type %s. Received value: '%s'",
                ex.getName(),
                requiredType,
                ex.getValue());

        MessageResponse errorResponse = new MessageResponse("FAILURE", detailMessage);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle optimistic locking conflicts
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<MessageResponse> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException ex,
            WebRequest request) {

        MessageResponse errorResponse = new MessageResponse(
                "FAILURE",
                "The resource was modified by another request. Please try again.");

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // Handle 404 not found exceptions
    @ExceptionHandler({
            NoHandlerFoundException.class,
            NoResourceFoundException.class
    })
    public ResponseEntity<MessageResponse> handleMappingAndResourceNotFound(
            Exception ex,
            WebRequest request) {

        String resourceIdentifier = "";

        if (ex instanceof NoHandlerFoundException) {
            resourceIdentifier = ((NoHandlerFoundException) ex).getRequestURL();
        } else if (ex instanceof NoResourceFoundException) {
            resourceIdentifier = ((NoResourceFoundException) ex).getResourcePath();
        }

        String detailMessage = String.format(
                "The requested resource '%s' does not exist.",
                resourceIdentifier);

        MessageResponse errorResponse = new MessageResponse("FAILURE", detailMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Handle method validation exceptions
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<MessageResponse> handleMethodValidation(HandlerMethodValidationException ex) {
        String errorMessage = ex.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return new ResponseEntity<>(new MessageResponse("FAILURE", errorMessage), HttpStatus.BAD_REQUEST);
    }

    // Handle constraint violation exceptions from validation annotations
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<MessageResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining("; "));

        return new ResponseEntity<>(new MessageResponse("FAILURE", errorMessage), HttpStatus.BAD_REQUEST);
    }

    // Handle unsupported HTTP method exceptions
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<MessageResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            WebRequest request) {

        String supportedMethods = String.join(", ", ex.getSupportedMethods());
        String detailMessage = String.format(
                "Request method '%s' is not supported. Supported methods are: %s",
                ex.getMethod(),
                supportedMethods);

        MessageResponse errorResponse = new MessageResponse("FAILURE", detailMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // Handle duplicate resource exceptions
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<MessageResponse> handleDuplicateResource(
            DuplicateResourceException ex,
            WebRequest request) {

        MessageResponse errorResponse = new MessageResponse("FAILURE", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // Handle illegal state exceptions
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<MessageResponse> handleIllegalState(
            IllegalStateException ex,
            WebRequest request) {

        MessageResponse errorResponse = new MessageResponse("FAILURE", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Catch-all handler for unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {

        ex.printStackTrace();

        MessageResponse errorResponse = new MessageResponse(
                "ERROR",
                "An unexpected internal error occurred. Please try again later.");

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}