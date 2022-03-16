package com.sin.simplecloud4u.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sin.simplecloud4u.model.entity.User;
import com.sin.simplecloud4u.service.interfa.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserServiceTest {
    @Autowired
    private UserService userService;
    @Test
    public void selectUserTest(){
        Page<User> page = PageHelper.startPage(0, 10);
        List<User> userList = userService.selectAllUser();
        PageInfo<User> pageInfo = new PageInfo<>(userList);
        System.out.println(userList);
    }
}
