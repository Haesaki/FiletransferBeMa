package com.sin.simplecloud4u.controller;

import com.sin.simplecloud4u.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class FileController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final StringRedisTemplate redisTemplate;

    private final ;

    @Autowired
    public FileController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/uploadTempFile")
    public String uploadTempFile(@RequestParam("file") MultipartFile file, String url) {
        File tempDirectory = new File(tempFilePath);
        if (!tempDirectory.exists())
            tempDirectory.mkdirs();

        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("tempFIle", file.getName()))) {
            session.setAttribute("msg", "文件名不符合标准，请修改文件名(在文件名末尾加上随机数字串)!!");
            return "redirect:/temp-file";
        }

        String remoteAddr = request.getRemoteAddr();
        // 单位是字节
        long fileSize = file.getSize();
        String code = RandomUtil.randomVerificationCode();
        // 未登录
        if (session.getAttribute("user") == null) {
            if (fileSize < VISITOR_MAX_FILE) {
                redisTemplate.opsForValue().set("tempFile", Objects.requireNonNull(file.getOriginalFilename()), Duration.ofDays(Long.parseLong(VISITOR_FILE_EXPIRED_TIME)));
                redisTemplate.opsForValue().set("sf2_" + file.getOriginalFilename(), code);
                logger.info("Not Login " + remoteAddr + " upload " + file.getOriginalFilename() + "-" + fileSize);
                allowedToCreateFile(file, url);
            } else {
                session.setAttribute("msg", "您的文件超出了文件上传大小的限制，请尝试上传小于" + VISITOR_MAX_FILE + "bits 的文件!!");
            }
        } else if (fileSize < REGISTERED_MAX_FILE) {
            // 不符合上面的判断的就是登录用户
            logger.info("Login " + remoteAddr + " upload " + file.getOriginalFilename() + "-" + String.valueOf(fileSize));
            redisTemplate.opsForValue().set("tempFile", Objects.requireNonNull(file.getOriginalFilename()), Duration.ofDays(Long.parseLong(REGISTERED_FILE_EXPIRED_TIME)));
            redisTemplate.opsForValue().set("sf2_" + file.getOriginalFilename(), code);
            logger.info("Login " + remoteAddr + " upload " + file.getOriginalFilename() + "-" + fileSize);
            allowedToCreateFile(file, url);
        } else {
            // 登录用户 文件大小超出了限制
            session.setAttribute("msg", "您的文件超出了文件上传大小的限制，请尝试上传小于" + String.valueOf(REGISTERED_MAX_FILE) + "bits 的文件!!");
        }
        return "redirect:/temp-file";
    }

    private void allowedToCreateFile(@RequestParam("file") MultipartFile file, String url) {
        try {
            if (!createFile(tempFilePath + file.getOriginalFilename(), file.getBytes()))
                session.setAttribute("msg", "文件错误！！！");
            else {
                url = url + "/file/share?name=" + Base64.getEncoder().encodeToString(Objects.requireNonNull(file.getOriginalFilename()).getBytes(StandardCharsets.UTF_8)) + "&flag=2";
                session.setAttribute("url", url);
                session.setAttribute("msg", "上传成功，访问链接 即可下载！");
            }
        } catch (IOException e) {
            e.printStackTrace();
            session.setAttribute("msg", "文件错误!!");
        }
    }

    private boolean createFile(String filePath, byte[] content) {
        File file = new File(filePath);
        if (file.exists())
            return false;
        try {
            if (!file.createNewFile())
                return false;
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // flag == 1 为用户文件分享
    //  链接包含文件名, 标志位, 用户id
    //  redis会存储该文件对应的在该用户的网盘里面的路径以及验证码 2天有效期
    //  文件存放在用户自定义的路径下面,通过userid和filename的注册在redis中查询值
    // flag == 2 临时文件分享
    //  链接包含文件名, 标志位
    //  redis只储存该文件对应的验证码
    //  文件存放在tempfile目录下
    @GetMapping("/file/share")
    public String getShareFile(@RequestParam(value = "userId", required = false) String userId,
                               @RequestParam(value = "name", required = true) String na,
                               @RequestParam(value = "flag", required = true) int flag,
                               @RequestParam(value = "verificationCode", required = true) String verificationCode) {
        String fileName = new String(Base64.getDecoder().decode(na), StandardCharsets.UTF_8);
        // 验证验证码
        String vCode = redisTemplate.opsForValue().get("sf2_" + fileName);
        if (vCode == null || !vCode.equals(verificationCode)) {
            // TODO: 禁止IP频繁访问
            session.setAttribute("msg", "链接或者验证码存在错误!");
            return "redirect:/error400Page";
        }
        try (OutputStream os = new BufferedOutputStream(response.getOutputStream());) {
            response.setCharacterEncoding("utf-8");
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
            if (flag == 1) {
                Resource resource =
                        File shareFile = new File(tempFilePath + fileName);
                if (!shareFile.exists()) {
                    session.setAttribute("msg", "链接或者验证码存在错误!");
                    return "redirect:/error400Page";
                }
            } else if (flag == 2) {
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

//    /**
//     * @Description 网盘的文件上传
//     * @Author xw
//     * @Date 23:10 2020/2/10
//     * @Param [files]
//     * @return java.util.Map<java.lang.String,java.lang.Object>
//     **/
//    @PostMapping("/uploadFile")
//    @ResponseBody
//    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile files) {
//        Map<String, Object> map = new HashMap<>();
//        if (fileStoreService.getFileStoreByUserId(loginUser.getUserId()).getPermission() != 0){
//            logger.error("用户没有上传文件的权限!上传失败...");
//            map.put("code", 499);
//            return map;
//        }
//        FileStore store = fileStoreService.getFileStoreByUserId(loginUser.getUserId());
//        Integer folderId = Integer.valueOf(request.getHeader("id"));
//        String name = files.getOriginalFilename().replaceAll(" ","");
//        //获取当前目录下的所有文件，用来判断是否已经存在
//        List<MyFile> myFiles = null;
//        if (folderId == 0){
//            //当前目录为根目录
//            myFiles = myFileService.getRootFilesByFileStoreId(loginUser.getFileStoreId());
//        }else {
//            //当前目录为其他目录
//            myFiles = myFileService.getFilesByParentFolderId(folderId);
//        }
//        for (int i = 0; i < myFiles.size(); i++) {
//            if ((myFiles.get(i).getMyFileName()+myFiles.get(i).getPostfix()).equals(name)){
//                logger.error("当前文件已存在!上传失败...");
//                map.put("code", 501);
//                return map;
//            }
//        }
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        String dateStr = format.format(new Date());
//        String path = loginUser.getUserId()+"/"+dateStr +"/"+folderId;
//        if (!checkTarget(name)){
//            logger.error("上传失败!文件名不符合规范...");
//            map.put("code", 502);
//            return map;
//        }
//        Integer sizeInt = Math.toIntExact(files.getSize() / 1024);
//        //是否仓库放不下该文件
//        if(store.getCurrentSize()+sizeInt > store.getMaxSize()){
//            logger.error("上传失败!仓库已满。");
//            map.put("code", 503);
//            return map;
//        }
//        //处理文件大小
//        String size = String.valueOf(files.getSize()/1024.0);
//        int indexDot = size.lastIndexOf(".");
//        size = size.substring(0,indexDot);
//        int index = name.lastIndexOf(".");
//        String tempName = name;
//        String postfix = "";
//        int type = 4;
//        if (index!=-1){
//            tempName = name.substring(index);
//            name = name.substring(0,index);
//            //获得文件类型
//            type = getType(tempName.toLowerCase());
//            postfix = tempName.toLowerCase();
//        }
//        try {
//            //提交到FTP服务器
//            boolean b = FtpUtil.uploadFile("/"+path, name + postfix, files.getInputStream());
//            if (b){
//                //上传成功
//                logger.info("文件上传成功!"+files.getOriginalFilename());
//                //向数据库文件表写入数据
//                myFileService.addFileByFileStoreId(
//                        MyFile.builder()
//                                .myFileName(name).fileStoreId(loginUser.getFileStoreId()).myFilePath(path)
//                                .downloadTime(0).uploadTime(new Date()).parentFolderId(folderId).
//                                size(Integer.valueOf(size)).type(type).postfix(postfix).build());
//                //更新仓库表的当前大小
//                fileStoreService.addSize(store.getFileStoreId(),Integer.valueOf(size));
//                try {
//                    Thread.sleep(5000);
//                    map.put("code", 200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }else{
//                logger.error("文件上传失败!"+files.getOriginalFilename());
//                map.put("code", 504);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return map;
//    }

    // 正则验证文件名是否合法 [汉字,字符,数字,下划线,英文句号,横线]
    public boolean checkTarget(String target) {
        final String format = "[^\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w-_.]";
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(target);
        return !matcher.find();
    }
}
