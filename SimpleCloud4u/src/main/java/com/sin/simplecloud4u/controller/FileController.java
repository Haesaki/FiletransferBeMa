package com.sin.simplecloud4u.controller;

import com.sin.simplecloud4u.model.entity.DirectoryEntity;
import com.sin.simplecloud4u.model.entity.User;
import com.sin.simplecloud4u.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class FileController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public FileController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/uploadTempFile")
    public String uploadTempFile(@RequestParam("file") MultipartFile file, String url, RedirectAttributes attributes) {
        File tempDirectory = new File(fileDirectory + tempFilePath);
        if (!tempDirectory.exists())
            tempDirectory.mkdirs();

        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("tempFIle", file.getName()))) {
            return "redirect:/error404Page";
        }

        // 客户端地址
        String remoteAddr = request.getRemoteAddr();
        // 单位是字节
        long fileSize = file.getSize();

        // 上传文件大小是否满足要求
        // 并且将过程塞到日志里面去
        if (session.getAttribute("user") == null) {
            if (fileSize > VISITOR_MAX_FILE)
                attributes.addFlashAttribute("msg", "您的文件超出了文件上传大小的限制，请尝试上传小于" + VISITOR_MAX_FILE + "bits 的文件!!");
            else
                logger.info("Not Login " + remoteAddr + " upload " + file.getOriginalFilename() + "-" + fileSize);
        } else {
            if (fileSize > REGISTERED_MAX_FILE)
                attributes.addFlashAttribute("msg", "您的文件超出了文件上传大小的限制，请尝试上传小于" + VISITOR_MAX_FILE + "bits 的文件!!");
            else
                logger.info("Login " + remoteAddr + " upload " + file.getOriginalFilename() + "-" + String.valueOf(fileSize));
        }

        // 将重要信息放到redis里面去记录
        String code = RandomUtil.randomVerificationCode();
        redisTemplate.opsForValue().set("tempFile", Objects.requireNonNull(file.getOriginalFilename()), Duration.ofDays(Long.parseLong(VISITOR_FILE_EXPIRED_TIME)));
        redisTemplate.opsForValue().set("sf2_" + file.getOriginalFilename(), code);

        // 创建文件
        if (!allowedToCreateFile(file)) {
            attributes.addFlashAttribute("error", "创建文件出错");
        } else {
            url = url + "/file/share?name=" + Base64.getEncoder().encodeToString(Objects.requireNonNull(file.getOriginalFilename()).getBytes(StandardCharsets.UTF_8)) + "&flag=2";
            attributes.addFlashAttribute("shareInfo", url + " Code: " + code);
            attributes.addFlashAttribute("msg", "上传成功，访问链接 即可下载！");
        }
        return "redirect:/file/shareInfo";
    }

    // 刚刚上传了一份文件 链接 url -> http://127.0.0.1:7777/sc4u/file/share?name=d2FsbGhhdmVuLThva3kxai5qcGc=&flag=2
    private boolean allowedToCreateFile(@RequestParam("file") MultipartFile file) {
        File uploadFile = new File(fileDirectory + tempFilePath + file.getOriginalFilename());
        try {
            if (!uploadFile.createNewFile())
                return false;
            FileOutputStream fileOutputStream = new FileOutputStream(uploadFile);
            fileOutputStream.write(file.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 网盘的文件上传
     **/
    @PostMapping("/uploadFile")
    @ResponseBody
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> map = new HashMap<>();

        User loginUser = (User) session.getAttribute("loginUser");
        // 如果没有登录 或者不是admin的话，就直接退出
        if (loginUser == null || !loginUser.getRole()){
            map.put("code", 404);
            return map;
        }

        DirectoryEntity directory = (DirectoryEntity) session.getAttribute("directory");
        String fileName = file.getOriginalFilename();
        return map;
    }

    /**
     * 网盘的文件下载
     **/
    @GetMapping("/downloadFile")
    public String downloadFile(@RequestParam Integer fId){
        User loginUser = (User) session.getAttribute("loginUser");
        // 如果没有登录 或者不是admin的话，就直接退出
        if (loginUser == null || !loginUser.getRole()){
            logger.error("用户没有下载文件的权限!下载失败...");
            return "redirect:/error401Page";
        }

        //获取文件信息
        return "success";
    }

    // 正则验证文件名是否合法 [汉字,字符,数字,下划线,英文句号,横线]
    public boolean checkTarget(String target) {
        final String format = "[^\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w-_.]";
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(target);
        return !matcher.find();
    }
}
