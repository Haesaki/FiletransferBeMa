package com.sin.simplecloud4u.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;

@Component
public class MailClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailClient.class);

    private JavaMailSender mailSender;

    public MailClient(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${spring.mail.username}")
    private String fromWho;

    public boolean sendCode(String toWho, String code) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setSubject("Simple Cloud - 邮箱验证");
            helper.setText(
                    "<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "  <head>\n" +
                            "    <meta charset=\"utf-8\" />\n" +
                            "    <title>用户注册-邮箱验证</title>\n" +
                            "  </head>\n" +
                            "  <body>\n" +
                            "    <p>您好</p>\n" +
                            "    <p style=\"text-indent: 10px\">您现在正在注册Simple Cloud账号</p>\n" +
                            "    <p style=\"text-indent: 10px\">验证码: <span>" + code + "</span></p>\n" +
                            "    <p style=\"text-indent: 10px\">邮箱: <span> " + toWho + " </span></p>\n" +
                            "    <p>谢谢</p>\n" +
                            "    <hr />\n" +
                            "    <h5 style=\"color: black\">如果并非本人操作,请忽略本邮件</h5>\n" +
                            "  </body>\n" +
                            "</html>\n", true);
            helper.setFrom("qiuxin136@163.com");
            helper.setTo(toWho);
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
        mailSender.send(mimeMessage);
        return true;
    }

    public boolean sendMail(String toWho, String subject, String content) {
        LOGGER.info("Send mail to: " + toWho + ",subject: " + subject);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(fromWho);
            helper.setTo(toWho);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
