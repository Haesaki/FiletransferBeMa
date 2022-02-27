package com.sin.simplecloud4u.mapper;

import com.sin.simplecloud4u.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    // return: 影响行数
    int createUser(User user);

    // return: 影响行数
    int deleteUserByName(String name);

    // return: 影响行数
    int deleteUserById(int id);

    User selectUserById(int id);

    User selectUserByName(String name);

    User selectUserByEmail(String email);
}
