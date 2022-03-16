package com.sin.simplecloud4u.controller;

import com.sin.simplecloud4u.model.entity.User;
import com.sin.simplecloud4u.service.interfa.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
    @GetMapping("/manages-users")
    public String manageUsers(Map<String, Object> map, Integer cur) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser.getRole()) {
            //用于无访问权限
            logger.error("当前登录用户：" + loginUser.getName() + "无管理员权限！");
            return "redirect:/error401Page";
        }
        //获取当前查询的页数，如果为空，默认为0
        cur = (cur == null || cur < 0) ? 0 : cur;
        //获得统计信息
        FileStoreStatistics statistics = myFileService.getCountStatistics(loginUser.getFileStoreId());
        //分页获得20个用户信息
        Page<Object> page = PageHelper.startPage(cur, 20);
        List<UserToShow> users = userService.getUsers();
        map.put("statistics", statistics);
        map.put("users", users);
        map.put("page", page);
        map.put("usersCount", usersCount);
        logger.info("用户管理域的内容：" + map);
        return "admin/manage-users";
    }

    /**
     * @return java.lang.String
     * @Description 修改用户的权限和最大容量
     * @Author xw
     * @Date 18:20 2020/3/10
     * @Param [uId, pre, size]
     **/
    @GetMapping("/updateStoreInfo")
    @ResponseBody
    public String updateStoreInfo(Integer uId, Integer pre, Integer size) {
        Integer integer = fileStoreService.updatePermission(uId, pre, size * 1024);
        if (integer == 1) {
            //更新成功，返回200状态码
            logger.info("修改用户" + userService.queryById(uId).getUserName() + "：的权限和仓库大小成功！");
            return "200";
        } else {
            //更新失败，返回500状态码
            logger.error("修改用户" + userService.queryById(uId).getUserName() + "：的权限和仓库大小失败！");
            return "500";
        }
    }

    /**
     * @return java.lang.String
     * @Description 删除用户
     * @Author xw
     * @Date 18:44 2020/3/10
     * @Param [uId, cur]
     **/
    @GetMapping("/deleteUser")
    public String deleteUser(Integer uId, Integer cur) {
        cur = (cur == null || cur < 0) ? 1 : cur;
        User user = userService.queryById(uId);
        FileStore fileStore = fileStoreService.getFileStoreByUserId(uId);
        List<FileFolder> folders = fileFolderService.getRootFoldersByFileStoreId(fileStore.getFileStoreId());
        //迭代删除文件夹
        for (FileFolder f : folders) {
            deleteFolderF(f);
        }
        List<MyFile> files = myFileService.getRootFilesByFileStoreId(fileStore.getFileStoreId());
        //删除该用户仓库根目录下的所有文件
        for (MyFile f : files) {
            String remotePath = f.getMyFilePath();
            String fileName = f.getMyFileName() + f.getPostfix();
            //从FTP文件服务器上删除文件
            boolean b = FtpUtil.deleteFile("/" + remotePath, fileName);
            if (b) {
                //删除成功,返回空间
                fileStoreService.subSize(f.getFileStoreId(), Integer.valueOf(f.getSize()));
                //删除文件表对应的数据
                myFileService.deleteByFileId(f.getMyFileId());
            }
            logger.info("删除文件成功!" + f);
        }
        if (FtpUtil.deleteFolder("/" + uId)) {
            logger.info("清空FTP上该用户的文件成功");
        } else {
            logger.error("清空FTP上该用户的文件失败");
        }
        userService.deleteById(uId);
        fileStoreService.deleteById(fileStore.getFileStoreId());
        return "redirect:/manages-users?cur=" + cur;
    }
}
