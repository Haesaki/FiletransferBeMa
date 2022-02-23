package com.sin.simplecloud4u.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisOperationTest {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedisList() {
        stringRedisTemplate.opsForValue().set("URL", "CAPTCHA", Duration.ofDays(1));
        String value = stringRedisTemplate.opsForValue().getAndExpire("URL", 1, TimeUnit.MILLISECONDS);
        Assertions.assertEquals("CAPTCHA", value);
    }
}