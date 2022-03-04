package com.sin.simplecloud4u.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class UtilTest {
    @Test
    public void Base64Test() {
        String[] testStrs = new String[]{"我叼.什么2222", "什么鬼.haha", "蛇蛇/shenme.鬼", "b站.mp4", "**poll.jpg", "什32么东12西22.apk"};
        String[] encoder = new String[testStrs.length], decoder = new String[testStrs.length];
        for (int i = 0; i < testStrs.length; i++) {
            encoder[i] = Base64.getEncoder().encodeToString(testStrs[i].getBytes(StandardCharsets.UTF_8));
        }

        for (int i = 0; i < testStrs.length; i++) {
            decoder[i] = new String(Base64.getDecoder().decode(encoder[i]), StandardCharsets.UTF_8);
            Assertions.assertEquals(testStrs[i], decoder[i]);
        }
    }
}
