package com.zerooneblog.api.interfaces.exception;

import com.zerooneblog.api.interfaces.dto.MessageResponse;

import jakarta.validation.ConstraintViolationException;
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
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
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
                .map(e -> String.format("%s: %s", e.getKey(), e.getValue()))
                .collect(Collectors.joining("; "));

        MessageResponse errorResponse = new MessageResponse(
                "FAILURE",
                detailedMessage);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {

        MessageResponse errorResponse = new MessageResponse("FAILURE", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<MessageResponse> handleNotificationNotFound(
            NotificationNotFoundException ex,
            WebRequest request) {

        MessageResponse errorResponse = new MessageResponse("FAILURE", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<MessageResponse> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        MessageResponse errorResponse = new MessageResponse(
                "FAILURE",
                "You don't have permission to access this resource.");
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<MessageResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        MessageResponse errorResponse = new MessageResponse(
                "FAILURE",
                "The file or content size is too large.");

        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }
    

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<MessageResponse> handleAuthenticationException(AuthenticationException ex) {

        // System.err.println("Authentication Failed: " + ex.getMessage());

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

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<MessageResponse> handleMethodValidation(HandlerMethodValidationException ex) {
        String errorMessage = ex.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return new ResponseEntity<>(new MessageResponse("FAILURE", errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<MessageResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining("; "));

        return new ResponseEntity<>(new MessageResponse("FAILURE", errorMessage), HttpStatus.BAD_REQUEST);
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