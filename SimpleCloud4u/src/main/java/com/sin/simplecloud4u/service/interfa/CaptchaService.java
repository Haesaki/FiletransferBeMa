package com.sin.simplecloud4u.service.interfa;

public interface CaptchaService {
    boolean isValidCaptcha(String url, String captcha);

    boolean setCaptcha(String url, String captcha);
}
