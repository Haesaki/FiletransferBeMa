package com.sin.simplecloud4u.controller;

import com.sin.simplecloud4u.model.entity.DirectoryEntity;
import com.sin.simplecloud4u.model.entity.FileStatistics;
import com.sin.simplecloud4u.model.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.util.Map;

@Controller
public class IndexController extends BaseController {
    @Value("sc4u.account.registered.max-file-size")
    private static int MAX_FILE_SIZE;

    @GetMapping("/user/index")
    public String index(Map<String, Object> map) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null)
            return "/";

        //获得统计信息
        String directoryPath = fileDirectory + "/" + loginUser.getId();

        // 判断当前的用户是不是存在目录
        File userDirectory = new File(directoryPath);
        if (!userDirectory.exists())
            userDirectory.mkdirs();

        DirectoryEntity directory = new DirectoryEntity("", true);
        session.setAttribute("directory", directory);

        FileStatistics fileStatistics = new FileStatistics();
        fileStatistics.MAX_SIZE = REGISTERED_FILE_MAX_SIZE;

        map.put("statistics", fileStatistics);

        map.put("MAX_FILE_SIZE", MAX_FILE_SIZE);
        return "/user/index";
    }

    @GetMapping("/user/files")
    public String getFileDirectory(Model model) {
        User user = (User) session.getAttribute("loginUser");

        return "/user/files";
    }
}
