package jv.supermarket.shared;

import java.time.Instant;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jv.supermarket.shared.customexception.AlreadyExistException;
import jv.supermarket.shared.customexception.BadAuthRequestException;
import jv.supermarket.shared.customexception.ImageSavingException;
import jv.supermarket.shared.customexception.OutOfStockException;
import jv.supermarket.shared.customexception.ResourceNotFoundException;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> resourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ArrayList<String> details = new ArrayList<>();
        details.add(e.getMessage());
        ApiError error = new ApiError(Instant.now(), status.value(), "Resource not found", request.getRequestURI(), details);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ApiError> alreadyExists(AlreadyExistException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ArrayList<String> details = new ArrayList<>();
        details.add(e.getMessage());
        ApiError error = new ApiError(Instant.now(), status.value(), "Resource already exists", request.getRequestURI(), details);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ImageSavingException.class)
    public ResponseEntity<ApiError> imageSavingError(ImageSavingException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ArrayList<String> details = new ArrayList<>();
        details.add(e.getMessage());
        ApiError error = new ApiError(Instant.now(), status.value(), "Image error", request.getRequestURI(), details);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalidArgument(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ArrayList<String> details = new ArrayList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            details.add(fieldError.getDefaultMessage());
        }
        ApiError error = new ApiError(Instant.now(), status.value(), "Validation error", request.getRequestURI(), details);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ApiError> outOfStock(OutOfStockException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ArrayList<String> details = new ArrayList<>();
        details.add(e.getMessage());
        ApiError error = new ApiError(Instant.now(), status.value(), "Insufficient stock", request.getRequestURI(), details);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> illegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ArrayList<String> details = new ArrayList<>();
        details.add(e.getMessage());
        ApiError error = new ApiError(Instant.now(), status.value(), "Invalid argument", request.getRequestURI(), details);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(BadAuthRequestException.class)
    public ResponseEntity<ApiError> authError(BadAuthRequestException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ArrayList<String> details = new ArrayList<>();
        details.add(e.getMessage());
        ApiError error = new ApiError(Instant.now(), status.value(), "Authentication error", request.getRequestURI(), details);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> accessDenied(AccessDeniedException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ArrayList<String> details = new ArrayList<>();
        details.add(e.getMessage());
        ApiError error = new ApiError(Instant.now(), status.value(), "Access denied", request.getRequestURI(), details);
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> redundantStateChange(IllegalStateException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ArrayList<String> details = new ArrayList<>();
        details.add(e.getMessage());
        ApiError error = new ApiError(Instant.now(), status.value(), "Unnecessary state change", request.getRequestURI(), details);
        return ResponseEntity.status(status).body(error);
    }

}
