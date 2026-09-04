package ai.openbuy.dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class DaoConfigTest {

    @Test
    void mybatisPlusIsOnClasspath() {
        assertDoesNotThrow(() -> Class.forName("com.baomidou.mybatisplus.core.mapper.BaseMapper"));
    }
}
