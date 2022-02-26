package com.sin.simplecloud4u.service.interfa;

// 服务会把验证码存储在Redis中，在从redis中读取出验证码
// 验证码 10 分钟有效
public interface VerificationCodeService {
    boolean sendVerificationEmail(String user);

    boolean verifiedCodeByUsername(String user, String code);
}
