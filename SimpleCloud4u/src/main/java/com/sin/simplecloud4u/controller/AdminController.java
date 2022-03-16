package com.sin.simplecloud4u.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sin.simplecloud4u.model.entity.User;
import com.sin.simplecloud4u.service.interfa.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(AdminController.class);

    private UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * @Description 前往用户管理页面
     **/
    @GetMapping("/user/manages-users")
    public String manageUsers(Map<String, Object> map, Integer cur) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (!loginUser.getRole()) {
            //用于无访问权限
            logger.error("当前登录用户：" + loginUser.getName() + "无管理员权限！");
            return "redirect:/error401Page";
        }
        //分页获得20个用户信息
        List<User> userList = userService.selectAllUser();
        map.put("users", userList);
        if (userList == null)
            map.put("usersCount", 0);
        else
            map.put("usersCount", userList.size());
        return "admin/userManager";
    }

    @GetMapping("/user/updateStoreInfo")
    @ResponseBody
    public String updateStoreInfo(Integer uId, Integer pre, Integer size) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (!loginUser.getRole()) {
            //用于无访问权限
            logger.error("当前登录用户：" + loginUser.getName() + "无管理员权限！");
            return "redirect:/error401Page";
        }
        Integer integer = userService.updatePermissionASize(uId, pre, size * 1024);
        if (integer == 1) {
            return "200";
        } else {
            return "500";
        }
    }

    @GetMapping("/deleteUser")
    public String deleteUser(Integer cur, Integer uId) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (!loginUser.getRole()) {
            //用于无访问权限
            logger.error("当前登录用户：" + loginUser.getName() + "无管理员权限！");
            return "redirect:/error401Page";
        }
        cur = (cur == null || cur < 0) ? 1 : cur;
        User user = userService.selectUserById(uId);
        String userDirectoryPath = fileDirectory + uId;
        deleteDirectory(userDirectoryPath);
        return "redirect:/manages-users?cur=" + cur;
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
}
