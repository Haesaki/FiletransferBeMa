package com.sin.simplecloud4u.service.implement;

import com.sin.simplecloud4u.service.interfa.CaptchaService;

public class CaptchaServiceMysql implements CaptchaService {
    @Override
    public boolean isValidCaptcha(String url, String captcha) {
        return false;
    }

    @Override
    public boolean setCaptcha(String url, String captcha) {
        return false;
    }
}
