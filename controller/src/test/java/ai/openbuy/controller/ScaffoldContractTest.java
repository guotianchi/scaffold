package ai.openbuy.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScaffoldContractTest {

    @Test
    void commonMarkerIsOnClasspath() {
        assertNotNull(ai.openbuy.common.CommonMarker.class);
    }

    @Test
    void mybatisPlusIsOnClasspath() {
        assertDoesNotThrow(() -> Class.forName("com.baomidou.mybatisplus.core.mapper.BaseMapper"));
    }

    @Test
    void jpaStarterIsNotOnClasspath() {
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("org.springframework.data.jpa.repository.JpaRepository"));
    }

    @Test
    void mysqlDatasourcePlaceholdersExist() throws Exception {
        try (var in = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.yml")) {
            assertNotNull(in);
            String yaml = new String(in.readAllBytes());
            assertTrue(yaml.contains("spring.datasource.url") || yaml.contains("url: jdbc:mysql://"));
            assertTrue(yaml.contains("username:"));
            assertTrue(yaml.contains("password:"));
        }
    }

    void testpr() {
    }
}
