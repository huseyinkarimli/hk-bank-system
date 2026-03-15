package az.hkbank.common.exception;

import az.hkbank.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the HK Bank System.
 * Catches exceptions thrown by controllers and converts them into standardized ApiResponse objects.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles BankException by returning the appropriate HTTP status and error message.
     *
     * @param ex the BankException
     * @return ResponseEntity with ApiResponse containing error details
     */
    @ExceptionHandler(BankException.class)
    public ResponseEntity<ApiResponse<Void>> handleBankException(BankException ex) {
        log.error("BankException: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        String message = ex.getDetail() != null 
                ? ex.getErrorCode().getMessage() + ": " + ex.getDetail()
                : ex.getErrorCode().getMessage();
        
        ApiResponse<Void> response = ApiResponse.error(message);
        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(response);
    }

    /**
     * Handles validation errors from @Valid annotations.
     *
     * @param ex the MethodArgumentNotValidException
     * @return ResponseEntity with ApiResponse containing field validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .timestamp(java.time.LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handles Spring Security access denied exceptions.
     *
     * @param ex the AccessDeniedException
     * @return ResponseEntity with ApiResponse containing forbidden error
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error("Access forbidden");
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    /**
     * Handles all other uncaught exceptions.
     *
     * @param ex the Exception
     * @return ResponseEntity with ApiResponse containing generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        
        ApiResponse<Void> response = ApiResponse.error("An unexpected error occurred");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
