package com.sin.simplecloud4u.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class FileController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Value("@{sc4u.tempFile.directory}")
    private String tempFilePath;

    @Value("@{sc4u.account.visitor.file-expired-time}")
    private int VISITOR_FILE_EXPIRED_TIME;

    @Value("@{sc4u.account.registered.file-expired-time}")
    private int REGISTERED_FILE_EXPIRED_TIME;

    private StringRedisTemplate redisTemplate;

    @Autowired
    public FileController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/uploadTempFile")
    public String uploadTempFile(@RequestParam("file") MultipartFile file, String url) {
        File tempDirectory = new File("");
        if (!tempDirectory.exists())
            tempDirectory.mkdir();

        if (redisTemplate.opsForSet().isMember("tempFIle", file.getName())) {
            session.setAttribute("msg", "文件名不符合标准，请修改文件名(在文件末尾加上随机数字串)!!");
            return "redirect:/temp-file";
        }

        String remoteAddr = request.getRemoteAddr();
        long fileSize = file.getSize();
        // 未登录
        if (session.getAttribute("user") == null) {
            if (fileSize < VISITOR_MAX_FILE) {
                redisTemplate.opsForValue().set("tempFile", file.getName(), Duration.ofDays(VISITOR_FILE_EXPIRED_TIME));
                logger.info("Not Login " + remoteAddr + " upload " + file.getName() + "-" + String.valueOf(fileSize));
            } else {
                session.setAttribute("msg", "您的文件超出了文件上传大小的限制，请尝试上传小于" + String.valueOf(VISITOR_MAX_FILE) + "bits 的文件!!");
            }
        } else if (fileSize < REGISTERED_MAX_FILE) {
            // 不符合上面的判断的就是登录用户
            logger.info("Login " + remoteAddr + " upload " + file.getName() + "-" + String.valueOf(fileSize));
        } else {
            // 登录用户 文件大小超出了限制
            session.setAttribute("msg", "您的文件超出了文件上传大小的限制，请尝试上传小于" + String.valueOf(REGISTERED_MAX_FILE) + "bits 的文件!!");
        }
        return "redirect:/temp-file";
    }

    // 正则验证文件名是否合法 [汉字,字符,数字,下划线,英文句号,横线]
    public boolean checkTarget(String target) {
        final String format = "[^\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w-_.]";
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(target);
        return !matcher.find();
    }
}
