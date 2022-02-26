package com.sin.simplecloud4u.service.interfa;

import com.sin.simplecloud4u.model.entity.User;

public interface UserService {
    void createUser(User user);

    void deleteUserByName(String name);

    void deleteUserById(int id);

    User selectUserById(int id);

    User selectUserByName(String name);

    User selectUserByEmail(String email);
}
