package com.sin.simplecloud4u.Service;

import com.sin.simplecloud4u.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class VerificationCodeServiceTest {
    @Autowired
    private MailClient mailClient;

    @Test
    public void mailTest(){
        mailClient.sendMail("laohapi@foxmail.com", "SendEmailTest", "This is a email test");
    }

    @Test
    public void sendCodeTest(){
        mailClient.sendCode("laohapi@gmail.com", "123123");
    }
}
