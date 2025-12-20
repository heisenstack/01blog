package com.zerooneblog.api.interfaces.exception;

import com.zerooneblog.api.interfaces.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.validation.FieldError;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String detailedMessage = errors.entrySet().stream()
                .map(e -> String.format("[%s: %s]", e.getKey(), e.getValue()))
                .collect(Collectors.joining("; "));

        MessageResponse errorResponse = new MessageResponse(
                "FAILURE",
                "Validation failed: " + detailedMessage);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<MessageResponse> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        MessageResponse errorResponse = new MessageResponse(
                "FAILURE",
                "You don't have permission to access this resource.");
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<MessageResponse> handleAuthenticationException(AuthenticationException ex) {

        System.err.println("Authentication Failed: " + ex.getMessage());

        MessageResponse errorResponse = new MessageResponse(
                "FAILURE",
                "Invalid username or password.");

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<MessageResponse> handleUnauthorizedAction(
            UnauthorizedActionException ex,
            WebRequest request) {

        MessageResponse errorResponse = new MessageResponse("FAILURE", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MessageResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {

        MessageResponse errorResponse = new MessageResponse("FAILURE", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

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

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<MessageResponse> handleDuplicateResource(
            DuplicateResourceException ex,
            WebRequest request) {

        MessageResponse errorResponse = new MessageResponse("FAILURE", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<MessageResponse> handleIllegalState(
            IllegalStateException ex,
            WebRequest request) {

        MessageResponse errorResponse = new MessageResponse("FAILURE", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

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