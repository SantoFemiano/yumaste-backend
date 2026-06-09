package com.yumaste.yumasteapi.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Test
    @DisplayName("BusinessException - messaggio corretto")
    void messageIsPreserved() {
        BusinessException ex = new BusinessException("Operazione non consentita");
        assertThat(ex.getMessage()).isEqualTo("Operazione non consentita");
    }
}
