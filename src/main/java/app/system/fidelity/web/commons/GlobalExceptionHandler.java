package app.system.fidelity.web.commons;

import app.system.fidelity.domain.exceptions.BusinessException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            final BusinessException ex,
            final WebRequest request
    ) {
        log.warn("Business exception: {} - Path: {}", ex.getMessage(), request.getDescription(false));

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                "BUSINESS_ERROR"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            final MethodArgumentNotValidException ex
    ) {
        log.warn("Validation failed: {} errors", ex.getBindingResult().getErrorCount());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .data(errors)
                .error("Falha na validação dos dados")
                .code("VALIDATION_ERROR")
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            final RuntimeException ex,
            final WebRequest request
    ) {
        log.warn("Authentication failed: {} - Path: {}", ex.getMessage(), request.getDescription(false));

        ApiResponse<Void> response = ApiResponse.error(
                "Credenciais inválidas",
                "AUTHENTICATION_FAILED"
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericAuthenticationException(
            final AuthenticationException ex,
            final WebRequest request
    ) {
        log.warn("Authentication error: {} - Path: {}", ex.getMessage(), request.getDescription(false));

        ApiResponse<Void> response = ApiResponse.error(
                "Falha na autenticação",
                "AUTHENTICATION_ERROR"
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            final AccessDeniedException ex,
            final WebRequest request
    ) {
        log.warn("Access denied: {} - Path: {}", ex.getMessage(), request.getDescription(false));

        ApiResponse<Void> response = ApiResponse.error(
                "Acesso negado",
                "ACCESS_DENIED"
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwtException(
            final ExpiredJwtException ex,
            final WebRequest request
    ) {
        log.warn("JWT expired: Path: {}", request.getDescription(false));

        ApiResponse<Void> response = ApiResponse.error(
                "Token expirado",
                "TOKEN_EXPIRED"
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtException(
            final JwtException ex,
            final WebRequest request
    ) {
        log.warn("JWT error: {} - Path: {}", ex.getMessage(), request.getDescription(false));

        ApiResponse<Void> response = ApiResponse.error(
                "Token inválido",
                "INVALID_TOKEN"
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            final IllegalArgumentException ex,
            final WebRequest request
    ) {
        log.warn("Invalid argument: {} - Path: {}", ex.getMessage(), request.getDescription(false));

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                "INVALID_ARGUMENT"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(
            final IllegalStateException ex,
            final WebRequest request
    ) {
        log.error("Illegal state: {} - Path: {}", ex.getMessage(), request.getDescription(false));

        ApiResponse<Void> response = ApiResponse.error(
                "Erro no estado da aplicação",
                "ILLEGAL_STATE"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            final Exception ex,
            final WebRequest request
    ) {
        log.error("Unexpected error occurred - Path: {} - Error: {}",
                request.getDescription(false), ex.getMessage(), ex);

        String message = "Ocorreu um erro inesperado";
        String code = "INTERNAL_ERROR";

        if (log.isDebugEnabled()) {
            message = ex.getMessage();
        }

        ApiResponse<Void> response = ApiResponse.error(message, code);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}