package com.sin.simplecloud4u.model.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserTest {
    @Test
    public void createDirectoryTest(){
        User user = new User("admin", "admin");
    }
}
