package com.sin.simplecloud4u.service.interfa;

import com.sin.simplecloud4u.model.entity.User;

public interface UserService {
    boolean createUser(User user);

    boolean deleteUserByName(String name);

    boolean deleteUserById(int id);

    User selectUserById(int id);

    User selectUserByName(String name);

    User selectUserByEmail(String email);
}
