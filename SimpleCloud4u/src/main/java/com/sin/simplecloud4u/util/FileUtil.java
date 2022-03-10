package com.sin.simplecloud4u.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;
import java.nio.file.Path;

public class FileUtil {
    public static Resource getFileAsResource(String path) {
        try {
            Resource resource = new UrlResource(path);
            if (resource.exists())
                return resource;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
