package com.sin.simplecloud4u.controller;

import com.sin.simplecloud4u.model.entity.DirectoryEntity;
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
import java.util.concurrent.TimeUnit;
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

        // ???????????????
        String remoteAddr = request.getRemoteAddr();
        // ???????????????
        long fileSize = file.getSize();

        // ????????????????????????????????????
        // ????????????????????????????????????
        if (session.getAttribute("user") == null) {
            if (fileSize > VISITOR_MAX_FILE)
                attributes.addFlashAttribute("msg", "????????????????????????????????????????????????????????????????????????" + VISITOR_MAX_FILE + "bits ?????????!!");
            else logger.info("Not Login " + remoteAddr + " upload " + file.getOriginalFilename() + "-" + fileSize);
        } else {
            if (fileSize > REGISTERED_MAX_FILE)
                attributes.addFlashAttribute("msg", "????????????????????????????????????????????????????????????????????????" + VISITOR_MAX_FILE + "bits ?????????!!");
            else
                logger.info("Login " + remoteAddr + " upload " + file.getOriginalFilename() + "-" + String.valueOf(fileSize));
        }

        // ?????????????????????redis???????????????
        String code = RandomUtil.randomVerificationCode();
        redisTemplate.opsForValue().set("tempFile", Objects.requireNonNull(file.getOriginalFilename()), Duration.ofDays(Long.parseLong(VISITOR_FILE_EXPIRED_TIME)));
        redisTemplate.opsForValue().set("sf2_" + file.getOriginalFilename(), code);

        // ????????????
        if (!allowedToCreateFile(file, fileDirectory + tempFilePath + file.getOriginalFilename())) {
            attributes.addFlashAttribute("error", "??????????????????");
        } else {
            url = url + "/file/share?name=" + Base64.getEncoder().encodeToString(Objects.requireNonNull(file.getOriginalFilename()).getBytes(StandardCharsets.UTF_8)) + "&flag=2";
            attributes.addFlashAttribute("shareInfo", url + " Code: " + code);
            attributes.addFlashAttribute("msg", "??????????????????????????? ???????????????");
        }
        return "redirect:/file/shareInfo";
    }

    // ??????????????????????????? ?????? url -> http://127.0.0.1:7777/sc4u/file/share?name=d2FsbGhhdmVuLThva3kxai5qcGc=&flag=2
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
     * ?????????????????????
     **/
    @PostMapping("/user/files")
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("uploadFolderPath") String uploadFolderPath, @RequestParam("folderId") int folderId, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");

        // ?????????????????? ????????????admin???????????????????????????????????????????????????
        if (loginUser == null || !loginUser.getRole()
                || !folderBelong(loginUser.getId(), uploadFolderPath)
                || !allowedToCreateFile(file, uploadFolderPath + file.getOriginalFilename())
                || file.getSize() > REGISTERED_STORAGE_MAX_SIZE) {
            model.addAttribute("error", "???????????? / ??????????????????????????????");
            return "redirect:/error404Page";
        }
        DirectoryEntity homeDirectory = new DirectoryEntity(fileDirectory + loginUser.getId(), true);
        session.setAttribute("directory", homeDirectory);

        return "redirect:/user/files?fId=" + folderId;
    }

    /**
     * ?????????????????????
     **/
    @GetMapping("/downloadFile")
    public ResponseEntity<Resource> downloadFile(@RequestParam String folderPath, @RequestParam String fileName) throws FileNotFoundException {
        User loginUser = (User) session.getAttribute("loginUser");
        File downloadFile = new File(folderPath + fileName);
        // ?????????????????? ????????????admin????????????????????????
        if (loginUser == null || !loginUser.getRole() || !downloadFile.exists()
                || !folderBelong(loginUser.getId(), folderPath)) {
            logger.error("?????????????????????????????????!????????????...");
            return null;
        }
        InputStreamResource resource = new InputStreamResource(new FileInputStream(downloadFile));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFile.getName() + "\"").contentLength(downloadFile.length()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    /*
     * ????????????
     */
    @GetMapping("/user/deleteFile")
    public String deleteFile(@RequestParam Integer fileId, Integer folderId, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        DirectoryEntity homeDirectory = (DirectoryEntity) session.getAttribute("directory");
        // ?????????????????? ????????????admin???????????????????????????????????????????????????
        if (loginUser == null || !loginUser.getRole() || folderId >= homeDirectory.getDirectoryList().size()
                || !folderBelong(loginUser.getId(), homeDirectory.getDirectoryPath())) {
            model.addAttribute("error", "???????????? / ??????????????????????????????");
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
     * ???????????????
     */
    @GetMapping("/user/deleteFolder")
    public String deleteFolder(@RequestParam(value = "fId") Integer fId) {
        User loginUser = (User) session.getAttribute("loginUser");
        DirectoryEntity homeDirectory = (DirectoryEntity) session.getAttribute("directory");
        if (homeDirectory.getDirectoryList().size() <= fId || loginUser == null || !loginUser.getRole()
                || !folderBelong(loginUser.getId(), homeDirectory.getDirectoryPath()))
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
     * ???????????????????????????????????????????????????
     *
     * @param sPath ??????????????????????????????
     * @return ????????????????????????true???????????????false
     */
    public boolean deleteDirectory(String sPath) {
        //??????sPath?????????????????????????????????????????????????????????
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //??????dir???????????????????????????????????????????????????????????????
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        //?????????????????????????????????(???????????????)
        File[] files = dirFile.listFiles();
        assert files != null;
        for (File file : files) {
            //???????????????
            if (file.isFile()) {
                File deleteFile = new File(file.getAbsolutePath());
                flag = deleteFile.delete();
                if (!flag) break;
            } //???????????????
            else {
                flag = deleteDirectory(file.getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //??????????????????
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * ???????????????
     */
    @PostMapping("/user/addFolder")
    public String addFolder(@RequestParam(value = "folderName", required = true) String folderName, @RequestParam(value = "folderPath", required = true) String folderPath, @RequestParam(value = "parentFolderId", required = true) Integer parentFolderId, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null || !loginUser.getRole() || !folderBelong(loginUser.getId(), folderPath))
            return "redirect:/error404Page";

        File newFolder = new File(folderPath + folderName);
        if (!newFolder.mkdirs()) return "redirect:/error404Page";

        DirectoryEntity homeDirectory = new DirectoryEntity(fileDirectory + loginUser.getId(), true);
        session.setAttribute("directory", homeDirectory);

        return "redirect:/user/files?fId=" + parentFolderId;
    }

    // http://localhost:7777/user/getQrCode/?id=0&url=http://localhost:7777/user/sc4u
    // ????????????
    @GetMapping("/user/share/file")
    @ResponseBody
    public Map<String, String> userShareFile(@RequestParam(value = "folderPath") String folderPath,
                                             @RequestParam(value = "fileId") Integer fileId,
                                             @RequestParam(value = "remoteURL") String remoteURL,
                                             Model model) {
        Map<String, String> ret = new HashMap<>();
        if (folderPath == null || fileId == null || session.getAttribute("loginUser") == null)
            return ret;

        if (remoteURL.charAt(remoteURL.length() - 1) != '/')
            remoteURL = remoteURL + "/";
        File userDirectory = new File(folderPath);
        File userShareFile = null;
        File[] files = userDirectory.listFiles();
        int cnt = -1;
        assert files != null;
        for (File f : files) {
            if (f.isFile()) {
                cnt++;
                if (cnt == fileId) {
                    userShareFile = f;
                    break;
                }
            }
        }
        // ??????????????????????????????
        if (!userDirectory.exists() || !userDirectory.isDirectory() || userShareFile == null)
            return ret;
        //??????????????????redis?????????????????? ???????????????????????????
        if (redisTemplate.opsForValue().get("sf1_" + userShareFile.getName()) != null) {
            String code = redisTemplate.opsForValue().get("sf1_" + userShareFile.getName());
            remoteURL = remoteURL + "file/share?name=" + Base64.getEncoder().encodeToString(Objects.requireNonNull(userShareFile.getName()).getBytes(StandardCharsets.UTF_8)) + "&flag=1";
            ret.put("shareInfo", "url: " + remoteURL + "," + "Code: " + code);
            return ret;
        }
        // ?????????????????????redis???????????????
        String code = RandomUtil.randomVerificationCode();
        redisTemplate.opsForValue().set("sf1_" + userShareFile.getName(), code, 2, TimeUnit.DAYS);
        redisTemplate.opsForValue().set("sf1_path_" + userShareFile.getName(),
                folderPath + userShareFile.getName(), 2, TimeUnit.DAYS);
        remoteURL = remoteURL + "file/share?name=" + Base64.getEncoder().encodeToString(Objects.requireNonNull(userShareFile.getName()).getBytes(StandardCharsets.UTF_8)) + "&flag=1";
        ret.put("shareInfo", "url: " + remoteURL + "," + "Code: " + code);
        return ret;
    }

    public boolean folderBelong(int userId, String path) {
        if (path == null)
            return false;
        if (path.length() < fileDirectory.length() || !path.substring(0, fileDirectory.length()).equals(fileDirectory))
            return false;
        String userDirectory = path.substring(fileDirectory.length());
        while (userDirectory.charAt(0) == '/' && userDirectory.length() != 0) {
            userDirectory = userDirectory.substring(1);
        }
        String[] directorys = userDirectory.split("/");
        if (directorys.length < 1 || !directorys[0].equals(String.valueOf(userId)))
            return false;
        return true;
    }

    // ????????????????????????????????? [??????,??????,??????,?????????,????????????,??????]
    public boolean checkTarget(String target) {
        final String format = "[^\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w-_.]";
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(target);
        return !matcher.find();
    }
}
