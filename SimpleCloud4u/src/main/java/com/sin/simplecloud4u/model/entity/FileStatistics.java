package com.sin.simplecloud4u.model.entity;

import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;

public class FileStatistics implements Serializable {
    public FileStatistics() {
        this.doc = 0;
        this.music = 0;
        this.video = 0;
        this.image = 0;
        this.other = 0;
        this.fileCount = 0;
        this.folderCount = 0;
        this.currentSize = 0;
    }

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

    public long MAX_SIZE;

    private FileStore fileStore;
}
