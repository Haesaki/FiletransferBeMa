package com.sin.simplecloud4u.service.implement;

import com.sin.simplecloud4u.mapper.UserMapper;
import com.sin.simplecloud4u.model.entity.User;
import com.sin.simplecloud4u.service.interfa.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// authenticate 认证
// authorize    授权
@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public boolean createUser(User user) {
        return userMapper.createUser(user) == 1;
    }

    @Override
    public boolean deleteUserByName(String name) {
        return userMapper.deleteUserByName(name) == 1;
    }

    @Override
    public boolean deleteUserById(int id) {
        return userMapper.deleteUserById(id) == 1;
    }

    @Override
    public User selectUserById(int id) {
        return userMapper.selectUserById(id);
    }

    @Override
    public User selectUserByName(String name) {
        return userMapper.selectUserByName(name);
    }

    @Override
    public User selectUserByEmail(String email) {
        return userMapper.selectUserByEmail(email);
    }
}
