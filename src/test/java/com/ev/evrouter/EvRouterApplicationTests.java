package com.ev.evrouter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "openrouteservice.api.key=test-key",
    "openchargemap.api.key=test-key"
})
class EvRouterApplicationTests {

    @Test
    void contextLoads() {
    }

}
