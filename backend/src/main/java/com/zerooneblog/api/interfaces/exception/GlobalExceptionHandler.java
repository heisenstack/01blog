package com.zerooneblog.api.interfaces.exception;

import com.zerooneblog.api.interfaces.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 403 Forbidden: UnauthorizedActionException ---
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<MessageResponse> handleUnauthorizedAction(
            UnauthorizedActionException ex, 
            WebRequest request) {
        
        MessageResponse errorResponse = new MessageResponse(
            "FAILURE", 
            ex.getMessage() 
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // 404 Not Found: ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MessageResponse> handleResourceNotFound(
            ResourceNotFoundException ex, 
            WebRequest request) {
        
        MessageResponse errorResponse = new MessageResponse(
            "FAILURE",
            ex.getMessage() 
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // 404 Not Found: NoHandlerFoundException
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<MessageResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, 
            WebRequest request) { 
        
        // System.err.println("No Handler Found: " + ex.getMessage());
        
        String detailMessage = String.format(
            "The requested resource '%s' does not exist.", 
            ex.getRequestURL()
        );

        MessageResponse errorResponse = new MessageResponse("FAILURE", detailMessage); 
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    // 405 Method Not Allowed: HttpRequestMethodNotSupportedException
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<MessageResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, 
            WebRequest request) { 
        
        // System.err.println("Method Not Allowed: " + ex.getMessage());
        
        String supportedMethods = String.join(", ", ex.getSupportedMethods());
        String detailMessage = String.format(
            "Request method '%s' is not supported. Supported methods are: %s", 
            ex.getMethod(), 
            supportedMethods
        );

        MessageResponse errorResponse = new MessageResponse("FAILURE", detailMessage); 
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<MessageResponse> handleNoResourceFound(
        NoResourceFoundException ex, 
        WebRequest request) {
    
    String detailMessage = String.format(
        "The requested resource '%s' does not exist.", 
        ex.getResourcePath()
    );

    MessageResponse errorResponse = new MessageResponse("FAILURE", detailMessage); 
    
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
}


    // 409 Conflict: DuplicateResourceException 
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<MessageResponse> handleDuplicateResource(
            DuplicateResourceException ex, 
            WebRequest request) {
    
        MessageResponse errorResponse = new MessageResponse(
            "FAILURE",
            ex.getMessage() 
        );
    
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    // 500 Internal Server Error: General Exception Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGlobalException(
            Exception ex, 
            WebRequest request) {
        
        ex.printStackTrace(); 
        
        MessageResponse errorResponse = new MessageResponse(
            "ERROR", 
            "An unexpected internal error occurred. Please try again later."
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}