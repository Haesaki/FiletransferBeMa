package com.sin.simplecloud4u.controller;

import com.sin.simplecloud4u.model.entity.FileStatistics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class IndexController extends BaseController {
    @Value("sc4u.account.registered.max-file-size")
    private static int MAX_FILE_SIZE;

    @GetMapping("/user/index")
    public String index(Map<String, Object> map) {
        //获得统计信息
        FileStatistics fileStatistics = new FileStatistics();
        map.put("statistics", fileStatistics);
        map.put("MAX_FILE_SIZE", MAX_FILE_SIZE);
        return "/user/index";
    }
}
