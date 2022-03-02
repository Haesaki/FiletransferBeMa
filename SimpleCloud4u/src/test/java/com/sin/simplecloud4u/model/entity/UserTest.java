package com.sin.simplecloud4u.model.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserTest {
    @Value("${sc4u.account.registered.max-file-size}")
    protected int REGISTERED_MAX_FILE;

    @Value("${sc4u.account.visitor.max-file-size}")
    protected int VISITOR_MAX_FILE;

    @Test
    public void createDirectoryTest() {
        Assertions.assertEquals(REGISTERED_MAX_FILE, 20971520);
        Assertions.assertEquals(VISITOR_MAX_FILE, 1048576);
    }
}
