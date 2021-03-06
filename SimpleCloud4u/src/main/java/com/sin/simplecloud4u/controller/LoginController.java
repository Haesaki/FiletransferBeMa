package com.sin.simplecloud4u.controller;

import com.sin.simplecloud4u.model.entity.DirectoryEntity;
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

import java.io.File;
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

    @PostMapping("/login")
    public String login(User user, Map<String, Object> map) {
        User userDB = userService.selectUserByEmail(user.getEmail());
        if (userDB != null && userDB.getPassword().equals(user.getPassword())) {
            userDB.setPassword("");
            session.setAttribute("loginUser", userDB);
            String directoryPath = fileDirectory + "/" + userDB.getId();
            File userDirectory = new File(directoryPath);
            if (!userDirectory.exists())
                userDirectory.mkdirs();
            DirectoryEntity directory = new DirectoryEntity(directoryPath, true);
            session.setAttribute("directory", directory);
            logger.info("???????????????" + userDB);
            return "redirect:/user/index";
        } else {
            String errorMsg = userDB == null ? "?????????????????????" : "????????????";
            logger.info("??????????????????????????????????????????????????????");
            //????????????????????????????????????????????????
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
            map.put("errorMsg", "???????????????");
            return "index";
        }
        // ??????????????????
        if (user.getName() != null)
            user.setName(user.getName().trim());
        else
            user.setName(UUID.randomUUID().toString());
        user.setRole(false);
        // default avatar https://p.qpic.cn/qqconnect/0/app_101851241_1582451550/100?max-age=2592000&t=0
        if (userService.createUser(user)) {

        } else {
            map.put("errorMsg", "????????????????????????????????????");
            return "index";
        }
//        if (userService.insert(user)) {
//            FileStore store = FileStore.builder().userId(user.getUserId()).currentSize(0).build();
//            fileStoreService.addFileStore(store);
//            user.setFileStoreId(store.getFileStoreId());
//            userService.update(user);
//            logger.info("???????????????????????????????????????" + user);
//            logger.info("???????????????????????????????????????" + store);
//        } else {
//            map.put("errorMsg", "????????????????????????????????????");
//            return "index";
//        }
        session.removeAttribute(user.getEmail() + "_code");
        session.setAttribute("loginUser", user);
        return "redirect:/index";
    }

    /**
     * @return void
     * @Description ??????????????????????????????, ??????????????????????????????
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
            logger.error("?????????????????????????????????????????????");
            return "exitEmail";
        }
        logger.info(" ?????????????????????: " + email);
        MailClient mailClient = new MailClient(mailSender);
        String code = RandomUtil.randomVerificationCode();
        if (mailClient.sendCode(email, code)) {
            // ???Code??????Redis???????????????????????????
            stringRedisTemplate.opsForValue().set(email + "_c", code, Duration.ofMinutes(10));
            // ???Code??????Model??????????????????
            model.addAttribute(email + "_code", code);
            return "success";
        }
        return "exitEmail";
    }

    @GetMapping("/logout")
    public String logout() {
        logger.info("?????????????????????");
        session.invalidate();
        return "redirect:/";
    }
}