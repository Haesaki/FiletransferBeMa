package com.sin.simplecloud4u.model.entity;

import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;

public class FileStatistics implements Serializable {
    /**
     * 文档数
     */
    public int doc;
    /**
     * 音乐数
     */
    public int music;
    /**
     * 视频数
     */
    public int video;
    /**
     * 图像数
     */
    public int image;
    /**
     * 其他
     */
    public int other;
    /**
     * 文件数
     */
    public int fileCount;
    /**
     * 文件夹数
     */
    public int folderCount;

    public long currentSize;

    @Value("sc4u.account.registered.max-file-size")
    public long MAX_SIZE;
}
