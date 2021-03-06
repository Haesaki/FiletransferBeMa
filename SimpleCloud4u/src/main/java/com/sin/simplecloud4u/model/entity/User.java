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

    private int permission;

    private int size;

    // false 普通用户 true admin
    private boolean role;

    @Value("${sc4u.account.directory-default-path:/sc4u/user/}")
    private String directoryPath;

    public User() {
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public boolean createDirectory() {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return true;
    }

    public boolean getRole() {
        return role;
    }
}
