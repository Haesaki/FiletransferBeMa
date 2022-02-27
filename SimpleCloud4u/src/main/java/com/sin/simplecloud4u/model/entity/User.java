package com.sin.simplecloud4u.model.entity;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

@Data
public class User {
    private int id;

    private String name;

    private String email;

    private String password;

    @Value("${sc4u.account.directory-default-path:/sc4u/user/}")
    private String directoryPath;

    // false 普通用户 true admin
    private boolean role;

    public User(){}

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public boolean createDirectory(){
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return true;
    }
}
