package com.sin.simplecloud4u.model.entity;

import com.sin.simplecloud4u.util.TrieTree;
import lombok.Data;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

@Data
public class DirectoryEntity {
    private String directoryName;

    private String directoryPath;

    private List<DirectoryEntity> subDirectory;

    // 只保存文件名字 不是文件的路径
    private List<String> files;

    // 文件大小, 大小为KB
    private List<Integer> fileSize;

    // 如果是主目录的话
    // 我们需要利用hash函数记录全部的文件 方便后续索引
    private boolean isHome;

    // 文件名 -> 文件地址
    // 方便后面进行搜索操作
    private TrieTree wordTree;

    // 文件统计数目
    private Integer fileCount;

    // 目录统计数目
    private Integer directoryCount;

    // directory id
    private List<DirectoryEntity> directoryList;

    // FolderId
    private Integer folderId;
    // parentID
    private Integer parentFolderId;

    private DirectoryEntity() {
    }

    // 只负责索引文件功能，不具备修改文件以及其他修改的操作
    // 传入的地址必须要求存在该目录
    // 只有初始化为主目录的才会初始化TrieTree
    public DirectoryEntity(String directoryPath, boolean isHome) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) return;
        // 如果路径末尾没有/ 就添加的链接的末尾
        if (directoryPath.charAt(directoryPath.length() - 1) == '/')
            this.directoryPath = directoryPath;
        else
            this.directoryPath = directoryPath + "/";

        this.fileCount = 0;
        this.directoryCount = 0;
        this.folderId = 0;
        this.parentFolderId = 0;

        this.directoryName = directory.getName();
        this.subDirectory = new LinkedList<>();
        this.files = new LinkedList<>();
        this.fileSize = new LinkedList<>();
        this.isHome = isHome;
        this.directoryList = new LinkedList<>();
        if (isHome) {
            this.wordTree = new TrieTree();
            this.directoryList.add(new DirectoryEntity());
        }
        // 每当初初始化一个目录实体的时候，会自动去扫描该目录下的全部文件
        this.iterateDirectory(wordTree);
    }

    // 不是主目录 不会初始化TrieTree, 初始化需要从上级传入TrieTree类,以便索引文件名
    public DirectoryEntity(String directoryPath, Integer folderId, Integer parentFolderId, TrieTree root) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) return;
        // 如果路径末尾没有/ 就添加的链接的末尾
        if (directoryPath.charAt(directoryPath.length() - 1) == '/')
            this.directoryPath = directoryPath;
        else
            this.directoryPath = directoryPath + "/";

        this.fileCount = 0;
        this.directoryCount = 0;
        this.folderId = folderId;
        this.parentFolderId = parentFolderId;

        this.directoryName = directory.getName();
        this.subDirectory = new LinkedList<>();
        this.files = new LinkedList<>();
        this.fileSize = new LinkedList<>();
        this.directoryList = new LinkedList<>();
        // 每当初初始化一个目录实体的时候，会自动去扫描该目录下的全部文件
        this.iterateDirectory(root);
    }

    public void iterateDirectory(TrieTree root) {
        File directory = new File(directoryPath);
        String[] sub = directory.list();
        if (sub != null) {
            for (String s : sub) {
                File subFile = new File(directoryPath + s);
                if (subFile.isDirectory()) {
                    this.directoryCount++;
                    DirectoryEntity directoryEntity = new DirectoryEntity(directoryPath + s,
                            this.folderId + this.subDirectory.size(),
                            this.folderId,
                            root);
                    this.directoryCount += directoryEntity.directoryCount;
                    this.fileCount += directoryEntity.fileCount;

                    directoryList.add(directoryEntity);
                    subDirectory.add(directoryEntity);
                } else {
                    this.fileCount++;
                    files.add(s);
                    long fileSizeB = new File(directoryPath + s).length();
                    int size = (int) (fileSizeB / 1024);
                    fileSize.add(size);
                    root.insert(s, directoryPath + s);
                }
            }
            for (DirectoryEntity directoryEntity : subDirectory) {
                directoryList.addAll(directoryEntity.getDirectoryList());
            }
        }
    }
}
