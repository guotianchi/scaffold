package ai.openbuy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HelloServiceTest {

    @Test
    void helloReturnsHello() {
        assertEquals("hello", new HelloService().hello());
    }
}
