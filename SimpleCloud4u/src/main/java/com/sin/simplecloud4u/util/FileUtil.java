package com.sin.simplecloud4u.util;

import com.sin.simplecloud4u.model.entity.FileType;
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

    public static int getFileType(String name) {
//        if (name.contains(".pdf") || name.contains(".docx") || name.contains(".doc"))
//            return FileType.DOC;
//        else if (name.contains(".mp3") || name.contains(".flac") || name.contains(".ogg") || name.contains(".m4a"))
//            return FileType.MUSIC;
//        else if (name.contains(".mp4") || name.contains(".av"))
//            return FileType.VIDEO;
//        else if (name.contains(".jpg") || name.contains(".png") || name.contains(".raw"))
//            return FileType.IMAGE;
//        return FileType.OTHER;
        if (name.contains(".pdf") || name.contains(".docx") || name.contains(".doc"))
            return 1;
        else if (name.contains(".jpg") || name.contains(".png") || name.contains(".raw"))
            return 2;
        else if (name.contains(".mp4") || name.contains(".av") || name.contains(".flv"))
            return 3;
        else if (name.contains(".mp3") || name.contains(".flac") || name.contains(".ogg") || name.contains(".m4a"))
            return 4;
        return 5;
    }
}
