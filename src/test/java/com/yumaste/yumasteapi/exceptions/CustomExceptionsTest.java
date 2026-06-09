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
        ResourceNotFoundException ex = new ResourceNotFoundException("Ingrediente non trovato");
        assertThat(ex.getMessage()).isEqualTo("Ingrediente non trovato");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("ResourceNotFoundException - annotazione @ResponseStatus 404")
    void resourceNotFoundException_has404Status() {
        ResponseStatus annotation = ResourceNotFoundException.class.getAnnotation(ResponseStatus.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("ConflictException - messaggio corretto")
    void conflictException_message() {
        ConflictException ex = new ConflictException("Email duplicata");
        assertThat(ex.getMessage()).isEqualTo("Email duplicata");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("ConflictException - annotazione @ResponseStatus 409")
    void conflictException_has409Status() {
        ResponseStatus annotation = ConflictException.class.getAnnotation(ResponseStatus.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("BusinessException - messaggio corretto")
    void businessException_message() {
        BusinessException ex = new BusinessException("Operazione non consentita");
        assertThat(ex.getMessage()).isEqualTo("Operazione non consentita");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("BusinessException - annotazione @ResponseStatus 400")
    void businessException_has400Status() {
        ResponseStatus annotation = BusinessException.class.getAnnotation(ResponseStatus.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("UnauthorizedException - messaggio corretto")
    void unauthorizedException_message() {
        UnauthorizedException ex = new UnauthorizedException("Non autorizzato");
        assertThat(ex.getMessage()).isEqualTo("Non autorizzato");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("UnauthorizedException - annotazione @ResponseStatus 403")
    void unauthorizedException_has403Status() {
        ResponseStatus annotation = UnauthorizedException.class.getAnnotation(ResponseStatus.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Le eccezioni custom devono poter essere lanciate e catturate")
    void exceptions_canBeThrownAndCaught() {
        assertThatThrownBy(() -> { throw new ResourceNotFoundException("not found"); })
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("not found");

        assertThatThrownBy(() -> { throw new ConflictException("conflict"); })
                .isInstanceOf(ConflictException.class)
                .hasMessage("conflict");

        assertThatThrownBy(() -> { throw new BusinessException("business"); })
                .isInstanceOf(BusinessException.class)
                .hasMessage("business");

        assertThatThrownBy(() -> { throw new UnauthorizedException("unauthorized"); })
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("unauthorized");
    }
}
