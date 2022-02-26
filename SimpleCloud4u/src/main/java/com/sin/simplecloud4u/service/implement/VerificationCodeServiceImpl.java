package com.sin.simplecloud4u.service.implement;

import com.sin.simplecloud4u.service.interfa.VerificationCodeService;

public class VerificationCodeServiceImpl implements VerificationCodeService {
    @Override
    public boolean sendVerificationEmail(String user) {
        return false;
    }

    @Override
    public boolean verifiedCodeByUsername(String user, String code) {
        return false;
    }
}
