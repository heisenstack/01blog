package com.zerooneblog.api.interfaces.exception;

import com.zerooneblog.api.interfaces.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;


@RestControllerAdvice
public class GlobalExceptionHandler {

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

    // --- 404 Not Found: ResourceNotFoundException ---
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

    // --- 409 Conflict: DuplicateResourceException ---
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
    
    // --- 500 Internal Server Error: General Exception Catch-all ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGlobalException(
            Exception ex, 
            WebRequest request) {
        
    
        
        MessageResponse errorResponse = new MessageResponse(
            "ERROR",
            "An unexpected internal error occurred. Please try again later."
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}