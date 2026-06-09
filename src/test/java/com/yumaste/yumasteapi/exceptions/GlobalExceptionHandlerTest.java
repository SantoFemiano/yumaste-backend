package com.yumaste.yumasteapi.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleResourceNotFound - restituisce 404")
    void handleResourceNotFound_returns404() {
        ResponseEntity<?> resp = handler.handleResourceNotFound(new ResourceNotFoundException("non trovato"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) resp.getBody();
        assertThat(body).containsEntry("error", "non trovato");
    }

    @Test
    @DisplayName("handleBusinessException - restituisce 409")
    void handleBusinessException_returns409() {
        ResponseEntity<?> resp = handler.handleBusinessException(new BusinessException("conflitto"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("handleAccessDeniedException - restituisce 403")
    void handleAccessDeniedException_returns403() {
        ResponseEntity<?> resp = handler.handleAccessDeniedException(new AccessDeniedException("vietato"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("handleBadCredentials - restituisce 401")
    void handleBadCredentials_returns401() {
        ResponseEntity<?> resp = handler.handleBadCredentials(new BadCredentialsException("credenziali errate"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("handleValidation - restituisce 400 con errori campi")
    void handleValidation_returns400() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "email", "non valida");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<?> resp = handler.handleValidation(ex);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) resp.getBody();
        assertThat(body).containsEntry("email", "non valida");
    }

    @Test
    @DisplayName("handleGenericException - restituisce 500")
    void handleGenericException_returns500() {
        ResponseEntity<?> resp = handler.handleGenericException(new RuntimeException("errore generico"));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
