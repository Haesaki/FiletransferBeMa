package com.sin.simplecloud4u.service.interfa;

import com.sin.simplecloud4u.model.entity.User;

public interface FileService {
    Integer addFileByUserFileName(User user, String name);

    Integer deleteFileByUserFileName(User user, String name);

    Integer getFileByUserFileName(User user, String name);
}
