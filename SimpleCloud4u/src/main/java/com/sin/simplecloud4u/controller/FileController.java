package com.sin.simplecloud4u.controller;

import com.sin.simplecloud4u.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
