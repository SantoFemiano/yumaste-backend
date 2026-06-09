package com.yumaste.yumasteapi.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomExceptionsTest {

    @Test
    @DisplayName("ResourceNotFoundException - messaggio corretto")
    void resourceNotFoundException_message() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Risorsa non trovata");
        assertThat(ex.getMessage()).isEqualTo("Risorsa non trovata");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("ResourceNotFoundException - annotazione @ResponseStatus 404")
    void resourceNotFoundException_responseStatus() {
        ResponseStatus annotation = ResourceNotFoundException.class.getAnnotation(ResponseStatus.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("BusinessException - messaggio corretto")
    void businessException_message() {
        BusinessException ex = new BusinessException("Operazione non permessa");
        assertThat(ex.getMessage()).isEqualTo("Operazione non permessa");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("ConflictException - messaggio corretto")
    void conflictException_message() {
        ConflictException ex = new ConflictException("Conflitto");
        assertThat(ex.getMessage()).isEqualTo("Conflitto");
    }

    @Test
    @DisplayName("UnauthorizedException - messaggio corretto")
    void unauthorizedException_message() {
        UnauthorizedException ex = new UnauthorizedException("Non autorizzato");
        assertThat(ex.getMessage()).isEqualTo("Non autorizzato");
    }

    @Test
    @DisplayName("ResourceNotFoundException - può essere lanciata e catturata")
    void resourceNotFoundException_throwAndCatch() {
        assertThatThrownBy(() -> { throw new ResourceNotFoundException("test"); })
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("test");
    }

    @Test
    @DisplayName("BusinessException - può essere lanciata e catturata")
    void businessException_throwAndCatch() {
        assertThatThrownBy(() -> { throw new BusinessException("test"); })
                .isInstanceOf(BusinessException.class)
                .hasMessage("test");
    }
}
