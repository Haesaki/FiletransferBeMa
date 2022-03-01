package com.sin.simplecloud4u.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class FileController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    @PostMapping("/uploadTempFile")
    public String uploadTempFile(@RequestParam("file") MultipartFile file, String url) {
        session.setAttribute("imgPath", "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2654852821,3851565636&fm=26&gp=0.jpg");
        String name = Objects.requireNonNull(file.getOriginalFilename()).replaceAll(" ", "");
        if (!checkTarget(name)) {
            logger.error("临时文件上传失败!文件名不符合规范...");
            session.setAttribute("msg", "上传失败!文件名不符合规范");
            return "redirect:/temp-file";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = format.format(new Date());
        String path = "temp/" + dateStr + "/" + UUID.randomUUID();
        try {
            if (FtpUtil.uploadFile("/" + path, name, file.getInputStream())) {
                //上传成功
                logger.info("临时文件上传成功!" + name);
                String size = String.valueOf(file.getSize());
                TempFile tempFile = TempFile.builder().fileName(name).filePath(path).size(size).uploadTime(new Date()).build();
                if (tempFileService.insert(tempFile)) {
                    try {
                        String id = UUID.randomUUID().toString();
                        String p = request.getSession().getServletContext().getRealPath("/user_img/");
                        Long t = tempFile.getUploadTime().getTime();
                        url = url + "/file/share?t=" + UUID.randomUUID().toString().substring(0, 10) + "&f=" + tempFile.getFileId() + "&p=" + size + "&flag=2";
                        File targetFile = new File(p, "");
                        if (!targetFile.exists()) {
                            targetFile.mkdirs();
                        }
                        File f = new File(p, id + ".jpg");
                        if (!f.exists()) {
                            //文件不存在,开始生成二维码并保存文件
                            OutputStream os = new FileOutputStream(f);
                            QRCodeUtil.encode(url, "/static/img/logo.png", os, true);
                            os.close();
                        }
                        //异步删除临时文件
                        tempFileService.deleteById(tempFile.getFileId());
                        session.setAttribute("imgPath", "user_img/" + id + ".jpg");
                        session.setAttribute("url", url);
                        session.setAttribute("msg", "上传成功，扫码/访问链接 即可下载！");
                        return "redirect:/temp-file";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    logger.info("临时文件数据库写入失败!" + name);
                    session.setAttribute("url", "error");
                    session.setAttribute("msg", "服务器出错了，临时文件上传失败!");
                }
            } else {
                //上传失败
                logger.info("临时文件上传失败!" + name);
                session.setAttribute("url", "error");
                session.setAttribute("msg", "服务器出错了，上传失败!");
            }
        } catch (IOException e) {
            e.printStackTrace();
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
