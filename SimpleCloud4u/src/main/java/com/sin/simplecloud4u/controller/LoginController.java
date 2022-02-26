package com.sin.simplecloud4u.controller;

import com.sin.simplecloud4u.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class LoginController {
    private Logger logger = LoggerFactory.getLogger(LoginController.class);

    /**
     * @return java.lang.String
     * @Description 免登陆用户入口，用于本地开发测试，上线运营为了安全请删除此方法
     **/
    @GetMapping("/admin")
    public String adminLogin(Model model) {
        User user = new User();
        logger.info("使用免登陆方式登录成功！" + user);
        model.addAttribute("loginUser", user);
        return "redirect:/index";
    }

    @RequestMapping("/login")
    public String loginRequest() {
        return "/login";
    }

    @PostMapping("/register")
    public String register(User user, String code, Map<String, Object> map, Model model) {
        String verificationCode = (String) model.getAttribute(user.getEmail() + "_code");
//        if (!code.equals(uCode)) {
//            map.put("errorMsg", "验证码错误");
//            return "index";
//        }
//        // 用户名去空格
//        user.setUserName(user.getUserName().trim());
//        // default avatar https://p.qpic.cn/qqconnect/0/app_101851241_1582451550/100?max-age=2592000&t=0
//        user.setRole(false);
//        if (userService.insert(user)) {
//            FileStore store = FileStore.builder().userId(user.getUserId()).currentSize(0).build();
//            fileStoreService.addFileStore(store);
//            user.setFileStoreId(store.getFileStoreId());
//            userService.update(user);
//            logger.info("注册用户成功！当前注册用户" + user);
//            logger.info("注册仓库成功！当前注册仓库" + store);
//        } else {
//            map.put("errorMsg", "服务器发生错误，注册失败");
//            return "index";
//        }
//        session.removeAttribute(user.getEmail() + "_code");
//        session.setAttribute("loginUser", user);
        return "redirect:/index";
    }
}