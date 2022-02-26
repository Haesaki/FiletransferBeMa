package com.sin.simplecloud4u.model.entity;

import lombok.Data;
import org.apache.tomcat.jni.Directory;

import java.util.List;

@Data
public class UserDirectory {
    // PATH 不包含当前目录名
    private String path;
    private String directoryName;
    private List<Directory> subDirectory;
    private List<String> files;

    public  UserDirectory(String directoryName, String path){
        this.directoryName = directoryName;
        this.path = path;
    }
}
