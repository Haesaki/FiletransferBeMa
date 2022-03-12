package com.sin.simplecloud4u.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
public class ShareFileController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(ShareFileController.class);

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public ShareFileController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/sc4u/file/share")
    public String getFileRequest(@RequestParam(value = "name", required = true) String na,
                                 @RequestParam(value = "flag", required = true) int flag,
                                 Model model) {
        String filename = new String(Base64.getDecoder().decode(na), StandardCharsets.UTF_8);
        model.addAttribute("filename", filename);
        return "/file/share";
    }

    // flag == 1 为用户文件分享
    //  链接包含文件名, 标志位, 用户id
    //  redis会存储该文件对应的在该用户的网盘里面的路径以及验证码 2天有效期
    //  文件存放在用户自定义的路径下面,通过userid和filename的注册在redis中查询值
    // flag == 2 临时文件分享
    //  链接包含文件名, 标志位
    //  redis只储存该文件对应的验证码
    //  文件存放在tempfile目录下
    // demo link http://localhost:7777/sc4u/file/share?name=OTU4Njk3NjZfcDAuanBn&flag=2 Code: 423535
    @PostMapping("/sc4u/file/share")
    public ResponseEntity<Resource> getShareFile(@RequestParam(value = "name", required = true) String na,
                                                 @RequestParam(value = "flag", required = true) int flag,
                                                 @RequestParam(value = "verificationCode", required = true) String verificationCode,
                                                 Model model) throws FileNotFoundException {
        String fileName = new String(Base64.getDecoder().decode(na), StandardCharsets.UTF_8);
        // 验证验证码
        String vCode = redisTemplate.opsForValue().get("sf2_" + fileName);
        String filePath = fileDirectory + tempFilePath + fileName;
        File downloadFile = new File(filePath);
        if (vCode == null || !vCode.equals(verificationCode) || !downloadFile.exists()) {
            // TODO: 禁止IP频繁访问
            model.addAttribute("msg", "链接或者验证码存在错误!");
            return null;
        }
        InputStreamResource resource = new InputStreamResource(new FileInputStream(downloadFile));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFile.getName() + "\"")
                .contentLength(downloadFile.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/file/shareInfo")
    public String shareInfo() {
        return "/file/shareInfo";
    }
}
