package com.sin.simplecloud4u.service.implement;

import com.sin.simplecloud4u.service.interfa.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CaptchaServiceRedis implements CaptchaService {
    final StringRedisTemplate redisTemplate;

    @Autowired
    public CaptchaServiceRedis(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isValidCaptcha(String url, String captcha) {
        if (url == null || captcha == null)
            return false;
        String val = redisTemplate.opsForValue().get(url);
        return val != null && captcha.equals(url);
    }

    @Override
    public boolean setCaptcha(String url, String captcha) {
        if (url == null || captcha == null)
            return false;
        redisTemplate.opsForValue().set(url, captcha);
        return true;
    }
}