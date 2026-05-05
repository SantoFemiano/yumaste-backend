package com.yumaste.yumasteapi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Disabilitato in CI perché mancano le variabili d'ambiente (DB, JWT)")
class YumasteApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
