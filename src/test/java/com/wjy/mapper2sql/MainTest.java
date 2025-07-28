package com.wjy.mapper2sql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author weijiayu
 * @date 2025/07/28
 */
public class MainTest {

    private Path testMapperFile;
    private Path outputDir;

    @BeforeEach
    void setUp() throws IOException {
        // 创建测试输出目录
        Path testOutputDir = Paths.get("target", "test-output");
        if (!Files.exists(testOutputDir)) {
            Files.createDirectories(testOutputDir);
        }

        // 使用test-classes目录下的资源文件
        testMapperFile = Paths.get("target", "test-classes", "test-mapper.xml");
        if (!Files.exists(testMapperFile)) {
            throw new IOException("测试文件不存在: " + testMapperFile.toAbsolutePath());
        }

        // 设置输出目录为test-output
        outputDir = Paths.get("target", "test-output");
    }

    @Test
    void testMainWithPropertiesFile() throws IOException {
        // 使用test-classes目录下的配置文件
        Path configFile = Paths.get("target", "test-classes", "test-config.properties");
        if (!Files.exists(configFile)) {
            throw new IOException("测试配置文件不存在: " + configFile.toAbsolutePath());
        }

        // 测试使用properties文件
        String[] args = { "--properties", configFile.toString() };
        assertDoesNotThrow(() -> {
            Main.main(args);
        });
    }

    @Test
    void testMainWithDirectParams() {
        // 测试直接参数
        String[] args = {
                "--work-dir", testMapperFile.getParent().toString(),
                "--output-dir", outputDir.toString(),
                "--db-type", "postgresql",
                "--mock", "true"
        };
        assertDoesNotThrow(() -> {
            Main.main(args);
        });
    }
}
