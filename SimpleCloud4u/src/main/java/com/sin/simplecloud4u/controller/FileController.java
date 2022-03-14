package com.sin.simplecloud4u.controller;

import com.sin.simplecloud4u.model.entity.DirectoryEntity;
import com.sin.simplecloud4u.model.entity.FileEntity;
import com.sin.simplecloud4u.model.entity.User;
import com.sin.simplecloud4u.util.RandomUtil;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
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
        if (!allowedToCreateFile(file, fileDirectory + tempFilePath + file.getOriginalFilename())) {
            attributes.addFlashAttribute("error", "创建文件出错");
        } else {
            url = url + "/file/share?name=" + Base64.getEncoder().encodeToString(Objects.requireNonNull(file.getOriginalFilename()).getBytes(StandardCharsets.UTF_8)) + "&flag=2";
            attributes.addFlashAttribute("shareInfo", url + " Code: " + code);
            attributes.addFlashAttribute("msg", "上传成功，访问链接 即可下载！");
        }
        return "redirect:/file/shareInfo";
    }

    // 刚刚上传了一份文件 链接 url -> http://127.0.0.1:7777/sc4u/file/share?name=d2FsbGhhdmVuLThva3kxai5qcGc=&flag=2
    private boolean allowedToCreateFile(@RequestParam("file") MultipartFile file, String path) {
        File uploadFile = new File(path);
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
    @PostMapping("/user/files")
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("uploadFolderPath") String uploadFolderPath, @RequestParam("folderId") int folderId, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");

        // 如果没有登录 或者不是admin的话，或者创建文件失败，就直接退出
        if (loginUser == null || !loginUser.getRole() || !allowedToCreateFile(file, uploadFolderPath + file.getOriginalFilename())) {
            model.addAttribute("error", "没有权限 / 创建文件过程存在错误");
            return "redirect:/error404Page";
        }
        DirectoryEntity homeDirectory = new DirectoryEntity(fileDirectory + loginUser.getId(), true);
        session.setAttribute("directory", homeDirectory);

        return "redirect:/user/files?fId=" + folderId;
    }

    /**
     * 网盘的文件下载
     **/
    @GetMapping("/downloadFile")
    public ResponseEntity<Resource> downloadFile(@RequestParam String folderPath, @RequestParam String fileName) throws FileNotFoundException {
        User loginUser = (User) session.getAttribute("loginUser");
        File downloadFile = new File(folderPath + fileName);
        // 如果没有登录 或者不是admin的话，就直接退出
        if (loginUser == null || !loginUser.getRole() || !downloadFile.exists()) {
            logger.error("用户没有下载文件的权限!下载失败...");
            return null;
        }
        InputStreamResource resource = new InputStreamResource(new FileInputStream(downloadFile));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFile.getName() + "\"")
                .contentLength(downloadFile.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /*
     * 删除文件
     */
    @GetMapping("/deleteFile")
    public String deleteFile(@RequestParam Integer fId, Integer folder) {
        return "redirect:/files?fId=" + folder;
    }

    /*
     * 删除文件夹
     */
    @GetMapping("/deleteFolder")
    public String deleteFolder(@RequestParam Integer fId) {
        User loginUser = (User) session.getAttribute("loginUser");
        DirectoryEntity homeDirectory = (DirectoryEntity) session.getAttribute("directory");
        if (homeDirectory.getDirectoryList().size() <= fId || loginUser == null || !loginUser.getRole())
            return "redirect:/error404Page";
        DirectoryEntity subDirectory = homeDirectory.getDirectoryList().get(fId);
        return "redirect:/files?fId=" + subDirectory.getParentFolderId();
    }

    /*
     * 添加文件夹
     */
    @PostMapping("/addFolder")
    public String addFolder(String folderName, int fId, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        DirectoryEntity homeDirectory = (DirectoryEntity) session.getAttribute("directory");
        if (homeDirectory.getDirectoryList().size() <= fId || loginUser == null || !loginUser.getRole())
            return "redirect:/error404Page";
        return "redirect:/files?fId=" + fId;
    }

    // 正则验证文件名是否合法 [汉字,字符,数字,下划线,英文句号,横线]
    public boolean checkTarget(String target) {
        final String format = "[^\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w-_.]";
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(target);
        return !matcher.find();
    }
}
