package ai.openbuy.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ScaffoldContractTest {

    @Test
    void coreMarkerIsOnClasspath() {
        assertNotNull(ai.openbuy.core.CoreMarker.class);
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
            org.junit.jupiter.api.Assertions.assertTrue(yaml.contains("spring.datasource.url") || yaml.contains("url: jdbc:mysql://"));
            org.junit.jupiter.api.Assertions.assertTrue(yaml.contains("username:"));
            org.junit.jupiter.api.Assertions.assertTrue(yaml.contains("password:"));
        }
    }
}
