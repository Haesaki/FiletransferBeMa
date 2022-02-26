package com.sin.simplecloud4u.mapper;

import com.sin.simplecloud4u.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    void createUser(User user);

    void deleteUserByName(String name);

    void deleteUserById(int id);

    User selectUserById(int id);

    User selectUserByName(String name);

    User selectUserByEmail(String email);
}
