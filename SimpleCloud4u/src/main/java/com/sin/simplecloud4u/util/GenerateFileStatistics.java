package com.sin.simplecloud4u.util;

import com.sin.simplecloud4u.model.entity.DirectoryEntity;
import com.sin.simplecloud4u.model.entity.FileStatistics;

public class GenerateFileStatistics {
    public static FileStatistics scanHomeDirectory(DirectoryEntity directory) {
        FileStatistics statistics = new FileStatistics();
        statistics.fileCount = directory.getFileCount();
        statistics.folderCount = directory.getDirectoryCount();
        statistics.currentSize = getTotalSize(directory);
        return statistics;
    }

    public static long getTotalSize(DirectoryEntity directory) {
        long size = 0;
        for (Integer i : directory.getFileSize())
            size += i;
        for (DirectoryEntity subDirectory : directory.getSubDirectory())
            size += getTotalSize(subDirectory);
        return size;
    }
}
