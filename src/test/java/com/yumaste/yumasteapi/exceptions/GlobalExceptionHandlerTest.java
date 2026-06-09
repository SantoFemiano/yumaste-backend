package com.yumaste.yumasteapi.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
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

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    @DisplayName("handleResourceNotFoundException - deve restituire 404")
    void handleResourceNotFoundException_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Risorsa non trovata");
        ResponseEntity<ErrorResponseDTO> response = handler.handleResourceNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("Risorsa non trovata");
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("handleUnauthorizedException - deve restituire 403")
    void handleUnauthorizedException_returns403() {
        UnauthorizedException ex = new UnauthorizedException("Accesso negato");
        ResponseEntity<ErrorResponseDTO> response = handler.handleUnauthorizedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(403);
        assertThat(response.getBody().message()).isEqualTo("Accesso negato");
        assertThat(response.getBody().error()).isEqualTo("Forbidden");
    }

    @Test
    @DisplayName("handleBusinessException - deve restituire 400")
    void handleBusinessException_returns400() {
        BusinessException ex = new BusinessException("Operazione non valida");
        ResponseEntity<ErrorResponseDTO> response = handler.handleBusinessException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("Operazione non valida");
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
    }

    @Test
    @DisplayName("handleConflictException - deve restituire 409")
    void handleConflictException_returns409() {
        ConflictException ex = new ConflictException("Email già esistente");
        ResponseEntity<ErrorResponseDTO> response = handler.handleConflictException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().message()).isEqualTo("Email già esistente");
        assertThat(response.getBody().error()).isEqualTo("Conflict");
    }

    @Test
    @DisplayName("handleValidationExceptions - deve restituire 400 con dettagli campi")
    void handleValidationExceptions_returns400WithFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("ingrediente", "nome", "Il nome è obbligatorio");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponseDTO> response = handler.handleValidationExceptions(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Validation Error");
        assertThat(response.getBody().message()).contains("nome");
    }

    @Test
    @DisplayName("handleGlobalException - deve restituire 500")
    void handleGlobalException_returns500() {
        Exception ex = new RuntimeException("Errore generico inaspettato");
        ResponseEntity<ErrorResponseDTO> response = handler.handleGlobalException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
    }

    @Test
    @DisplayName("ErrorResponseDTO - il timestamp deve essere non nullo")
    void errorResponseDTO_timestampNotNull() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Test");
        ResponseEntity<ErrorResponseDTO> response = handler.handleResourceNotFoundException(ex, request);

        assertThat(response.getBody().timestamp()).isNotNull();
    }
}
