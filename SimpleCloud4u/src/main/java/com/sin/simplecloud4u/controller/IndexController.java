package com.sin.simplecloud4u.controller;

import com.sin.simplecloud4u.model.entity.*;
import com.sin.simplecloud4u.util.FileUtil;
import org.apache.tomcat.jni.Directory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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

        DirectoryEntity directory = (DirectoryEntity) session.getAttribute("directory");

        FileStatistics fileStatistics = new FileStatistics();
        fileStatistics.MAX_SIZE = REGISTERED_STORAGE_MAX_SIZE;
        fileStatistics.fileCount = directory.getFileCount();
        fileStatistics.folderCount = directory.getDirectoryCount();

        map.put("statistics", fileStatistics);

        map.put("MAX_FILE_SIZE", MAX_FILE_SIZE);
        return "/user/index";
    }

    @GetMapping("/user/files")
    public String getFileDirectory(Integer fId, String filePath, Integer error, Map<String, Object> map) {
        //判断是否包含错误信息
        if (error != null) {
            if (error == 1) {
                map.put("error", "添加失败！当前已存在同名文件夹");
            }
            if (error == 2) {
                map.put("error", "重命名失败！文件夹已存在");
            }
        }
        DirectoryEntity homeDirectory = (DirectoryEntity) session.getAttribute("directory");
        List<DirectoryEntity> directoryList = homeDirectory.getDirectoryList();
        User loginUser = (User) session.getAttribute("loginUser");
        if (fId == null || fId <= 0 || (directoryList != null && fId >= directoryList.size())) {
            //代表当前为根目录
            fId = 0;
        }
        DirectoryEntity targetDirectory;
        if (fId == 0)
            targetDirectory = homeDirectory;
        else
            targetDirectory = directoryList.get(fId);

        //包含的子文件夹
        List<FileFolder> folders = new LinkedList<>();
        //包含的文件
        List<FileEntity> files = new LinkedList<>();
        //当前文件夹信息
        FileFolder nowFolder = new FileFolder(fId,
                targetDirectory.getDirectoryName(),
                targetDirectory.getDirectoryPath(),
                targetDirectory.getParentFolderId(),
                loginUser.getId(),
                new Date());
        //当前文件夹的相对路径
        List<FileFolder> location = new LinkedList<>();

        int cnt = 1;
        if (targetDirectory.getSubDirectory() != null)
            for (DirectoryEntity directory : targetDirectory.getSubDirectory()) {
                FileFolder fileFolder = new FileFolder(fId + cnt,
                        directory.getDirectoryName(),
                        directory.getDirectoryPath(),
                        fId,
                        loginUser.getId(),
                        new Date());
                folders.add(fileFolder);
                cnt++;
            }
        cnt = 0;
        if (targetDirectory.getFiles() != null) {
            List<Integer> size = targetDirectory.getFileSize();
            for (String s : targetDirectory.getFiles()) {
                FileEntity file = new FileEntity(cnt,
                        s,
                        loginUser.getId(),
                        homeDirectory.getDirectoryPath() + s,
                        0,
                        new Date(),
                        fId,
                        size.get(cnt),
                        FileUtil.getFileType(s),
                        s
                );
                files.add(file);
                cnt++;
            }
        }

        map.put("folders", folders);
        map.put("files", files);
        map.put("nowFolder", nowFolder);
        map.put("location", location);
        return "/user/files";
    }
}