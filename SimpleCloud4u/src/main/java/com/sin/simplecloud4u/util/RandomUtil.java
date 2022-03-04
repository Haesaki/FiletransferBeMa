package com.sin.simplecloud4u.util;

public class RandomUtil {

    public static String randomVerificationCode() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }
}
