package com.sin.simplecloud4u.service.implement;

import com.sin.simplecloud4u.model.entity.User;
import com.sin.simplecloud4u.service.interfa.FileService;
import org.springframework.core.io.Resource;

public class FileServiceImpl implements FileService {
    @Override
    public Integer addFileByUserFileName(User user, String name) {
        return null;
    }

    @Override
    public Integer deleteFileByUserFileName(User user, String name) {
        return null;
    }

    @Override
    public Integer getFileByUserFileName(User user, String name) {
        return null;
    }

    @Override
    public Integer getFileByPath(String path) {
        return null;
    }

    @Override
    public Resource getFileAsResource(String path) {
        return null;
    }
}
