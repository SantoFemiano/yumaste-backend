package com.yumaste.yumasteapi.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceNotFoundExceptionTest {

    @Test
    @DisplayName("ResourceNotFoundException - messaggio corretto")
    void messageIsPreserved() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Entità non trovata");
        assertThat(ex.getMessage()).isEqualTo("Entità non trovata");
    }
}
