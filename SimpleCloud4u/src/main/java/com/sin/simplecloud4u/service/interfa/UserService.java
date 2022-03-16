package com.sin.simplecloud4u.service.interfa;

import com.sin.simplecloud4u.model.entity.User;

import java.util.List;

public interface UserService {
    boolean createUser(User user);

    boolean deleteUserByName(String name);

    boolean deleteUserById(int id);

    Integer updatePermissionASize(int id, int permission, int size);

    User selectUserById(int id);

    User selectUserByName(String name);

    User selectUserByEmail(String email);

    List<User> selectAllUser();
}
