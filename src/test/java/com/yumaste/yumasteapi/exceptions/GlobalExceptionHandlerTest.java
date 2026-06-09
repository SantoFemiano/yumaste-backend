package com.yumaste.yumasteapi.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @Test
    @DisplayName("handleResourceNotFoundException - restituisce 404")
    void handleResourceNotFoundException_returns404() {
        when(request.getRequestURI()).thenReturn("/api/test");
        ResponseEntity<?> resp = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("non trovato"), request);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("handleUnauthorizedException - restituisce 403")
    void handleUnauthorizedException_returns403() {
        when(request.getRequestURI()).thenReturn("/api/test");
        ResponseEntity<?> resp = handler.handleUnauthorizedException(
                new UnauthorizedException("vietato"), request);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("handleBusinessException - restituisce 400")
    void handleBusinessException_returns400() {
        when(request.getRequestURI()).thenReturn("/api/test");
        ResponseEntity<?> resp = handler.handleBusinessException(
                new BusinessException("errore logico"), request);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("handleConflictException - restituisce 409")
    void handleConflictException_returns409() {
        when(request.getRequestURI()).thenReturn("/api/test");
        ResponseEntity<?> resp = handler.handleConflictException(
                new ConflictException("conflitto"), request);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("handleValidationExceptions - restituisce 400 con errori campi")
    void handleValidationExceptions_returns400() {
        when(request.getRequestURI()).thenReturn("/api/test");
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "email", "non valida");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<?> resp = handler.handleValidationExceptions(ex, request);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("handleGlobalException - restituisce 500")
    void handleGlobalException_returns500() {
        when(request.getRequestURI()).thenReturn("/api/test");
        ResponseEntity<?> resp = handler.handleGlobalException(
                new RuntimeException("errore generico"), request);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
