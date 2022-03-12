package com.sin.simplecloud4u.controller;

import com.sin.simplecloud4u.model.entity.User;
import com.sin.simplecloud4u.service.interfa.UserService;
import com.sin.simplecloud4u.util.MailClient;
import com.sin.simplecloud4u.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Controller
public class LoginController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final UserService userService;

    private final JavaMailSenderImpl mailSender;

    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public LoginController(UserService userService, JavaMailSenderImpl mailSender, StringRedisTemplate redisTemplate) {
        this.userService = userService;
        this.mailSender = mailSender;
        this.stringRedisTemplate = redisTemplate;
    }

    /**
     * @return java.lang.String
     * @Description 免登陆用户入口，用于本地开发测试，上线运营为了安全请删除此方法
     **/
    @GetMapping("/admin")
    public String adminLogin(Model model) {
        User user = new User();
        user.setName("admin");
        user.setPassword("admin");
        user.setRole(true);
        user.setEmail("admin");
        user.setId(0);

        logger.info("使用免登陆方式登录成功！" + user);
        model.addAttribute("loginUser", user);
        return "redirect:/user/index";
    }

    @PostMapping("/login")
    public String login(User user, Map<String, Object> map) {
        User userDB = userService.selectUserByEmail(user.getEmail());
        if (userDB != null && userDB.getPassword().equals(user.getPassword())) {
            userDB.setPassword("");
            session.setAttribute("loginUser", userDB);
            logger.info("登录成功！" + userDB);
            return "redirect:/user/index";
        } else {
            String errorMsg = userDB == null ? "该邮箱尚未注册" : "密码错误";
            logger.info("登录失败！请确认邮箱和密码是否正确！");
            //登录失败，将失败信息返回前端渲染
            map.put("errorMsg", errorMsg);
            return "index";
        }
    }

    @PostMapping("/register")
    public String register(User user, String code, Map<String, Object> map, Model model) {
        if (code == null || code.length() != 6)
            return "index";
        String verificationCode = (String) stringRedisTemplate.opsForValue().getAndDelete(user.getEmail() + "_c");
        if (!code.equals(verificationCode)) {
            map.put("errorMsg", "验证码错误");
            return "index";
        }
        // 用户名去空格
        if (user.getName() != null)
            user.setName(user.getName().trim());
        else
            user.setName(UUID.randomUUID().toString());
        user.setRole(false);
        // default avatar https://p.qpic.cn/qqconnect/0/app_101851241_1582451550/100?max-age=2592000&t=0
        if (userService.createUser(user)) {

        } else {
            map.put("errorMsg", "服务器发生错误，注册失败");
            return "index";
        }
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
        session.removeAttribute(user.getEmail() + "_code");
        session.setAttribute("loginUser", user);
        return "redirect:/index";
    }

    /**
     * @return void
     * @Description 向注册邮箱发送验证码, 并验证邮箱是否已使用
     * @Author xw
     * @Date 19:32 2020/1/29
     * @Param [userName, email, password]
     **/
    @ResponseBody
    @RequestMapping("/sendCode")
    public String sendCode(Model model, String userName, String email, String password, String invitationCode) {
        if (!"simplecloud4u".equals(invitationCode))
            return "exitEmail";
        User userByEmail = userService.selectUserByEmail(email);
        if (userByEmail != null) {
            logger.error("发送验证码失败！邮箱已被注册！");
            return "exitEmail";
        }
        logger.info(" 发送注册邮件到: " + email);
        MailClient mailClient = new MailClient(mailSender);
        String code = RandomUtil.randomVerificationCode();
        if (mailClient.sendCode(email, code)) {
            // 把Code丢到Redis储存池子中进行存储
            stringRedisTemplate.opsForValue().set(email + "_c", code, Duration.ofMinutes(10));
            // 把Code丢到Model，发送给前端
            model.addAttribute(email + "_code", code);
            return "success";
        }
        return "exitEmail";
    }

    @GetMapping("/logout")
    public String logout() {
        logger.info("用户退出登录！");
        session.invalidate();
        return "redirect:/";
    }
}