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
        if (!tempDirectory.exists()) tempDirectory.mkdirs();

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
            else logger.info("Not Login " + remoteAddr + " upload " + file.getOriginalFilename() + "-" + fileSize);
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
            if (!uploadFile.createNewFile()) return false;
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
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFile.getName() + "\"").contentLength(downloadFile.length()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    /*
     * 删除文件
     */
    @GetMapping("/user/deleteFile")
    public String deleteFile(@RequestParam Integer fileId, Integer folderId, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        DirectoryEntity homeDirectory = (DirectoryEntity) session.getAttribute("directory");
        // 如果没有登录 或者不是admin的话，或者创建文件失败，就直接退出
        if (loginUser == null || !loginUser.getRole() || folderId >= homeDirectory.getDirectoryList().size()) {
            model.addAttribute("error", "没有权限 / 创建文件过程存在错误");
            return "redirect:/error404Page";
        }

        DirectoryEntity nowDirectory;
        if (folderId == 0) nowDirectory = homeDirectory;
        else nowDirectory = homeDirectory.getDirectoryList().get(folderId);

        String folderPath = nowDirectory.getDirectoryPath();
        if (nowDirectory.getFiles() == null) return "redirect:/error404Page";
        String fileName = nowDirectory.getFiles().get(fileId);
        File file = new File(folderPath + fileName);

        if (!file.exists() || !file.isFile() || !file.delete()) return "redirect:/error404Page";

        homeDirectory = new DirectoryEntity(fileDirectory + loginUser.getId(), true);
        session.setAttribute("directory", homeDirectory);

        return "redirect:/user/files?fId=" + folderId;
    }

    /*
     * 删除文件夹
     */
    @GetMapping("/user/deleteFolder")
    public String deleteFolder(@RequestParam(value = "fId") Integer fId) {
        User loginUser = (User) session.getAttribute("loginUser");
        DirectoryEntity homeDirectory = (DirectoryEntity) session.getAttribute("directory");
        if (homeDirectory.getDirectoryList().size() <= fId || loginUser == null || !loginUser.getRole())
            return "redirect:/error404Page";

        DirectoryEntity subDirectory;
        if (fId == 0) subDirectory = homeDirectory;
        else subDirectory = homeDirectory.getDirectoryList().get(fId);

        File subD = new File(subDirectory.getDirectoryPath());
        if (!subD.exists() || !subD.isDirectory() || !deleteDirectory(subDirectory.getDirectoryPath()))
            return "redirect:/error404Page";

        homeDirectory = new DirectoryEntity(fileDirectory + loginUser.getId(), true);
        session.setAttribute("directory", homeDirectory);

        return "redirect:/user/files?fId=" + subDirectory.getParentFolderId();
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     *
     * @param sPath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public boolean deleteDirectory(String sPath) {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        assert files != null;
        for (File file : files) {
            //删除子文件
            if (file.isFile()) {
                File deleteFile = new File(file.getAbsolutePath());
                flag = deleteFile.delete();
                if (!flag) break;
            } //删除子目录
            else {
                flag = deleteDirectory(file.getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * 添加文件夹
     */
    @PostMapping("/user/addFolder")
    public String addFolder(@RequestParam(value = "folderName", required = true) String folderName, @RequestParam(value = "folderPath", required = true) String folderPath, @RequestParam(value = "parentFolderId", required = true) Integer parentFolderId, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null || !loginUser.getRole()) return "redirect:/error404Page";

        File newFolder = new File(folderPath + folderName);
        if (!newFolder.mkdirs()) return "redirect:/error404Page";

        DirectoryEntity homeDirectory = new DirectoryEntity(fileDirectory + loginUser.getId(), true);
        session.setAttribute("directory", homeDirectory);

        return "redirect:/user/files?fId=" + parentFolderId;
    }

    // http://localhost:7777/user/getQrCode/?id=0&url=http://localhost:7777/user/sc4u
    // share
    @GetMapping("/user/share/file")
    public String userShareFile() {

        return "redirect:/error400Page";
    }

    // 正则验证文件名是否合法 [汉字,字符,数字,下划线,英文句号,横线]
    public boolean checkTarget(String target) {
        final String format = "[^\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w-_.]";
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(target);
        return !matcher.find();
    }
}
