package com.sin.simplecloud4u.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ConfigTest {
    @Value("${sc4u.account.directory-default-path}")
    private String directoryPath;

    @Test
    public void configTest() {
        System.out.println(directoryPath);
    }
}