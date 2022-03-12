package com.sin.simplecloud4u.model.entity;

import com.sin.simplecloud4u.util.TrieTree;
import lombok.Data;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class DirectoryEntity {
    private String directoryName;

    private String directoryPath;

    private List<DirectoryEntity> subDirectory;

    // 只保存文件名字 不是文件的路径
    private List<String> files;

    // 如果是主目录的话
    // 我们需要利用hash函数记录全部的文件 方便后续索引
    private boolean isHome;

    // 文件名 -> 文件地址
    // 方便后面进行搜索操作
    private TrieTree wordTree;

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

        this.directoryName = directory.getName();
        this.subDirectory = new LinkedList<>();
        this.files = new LinkedList<>();
        this.isHome = isHome;
        if (isHome)
            this.wordTree = new TrieTree();
        // 每当初初始化一个目录实体的时候，会自动去扫描该目录下的全部文件
        this.iterateDirectory(wordTree);
    }

    // 不是主目录 不会初始化TrieTree, 初始化需要从上级传入TrieTree类,以便索引文件名
    public DirectoryEntity(String directoryPath, TrieTree root) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) return;
        // 如果路径末尾没有/ 就添加的链接的末尾
        if (directoryPath.charAt(directoryPath.length() - 1) == '/')
            this.directoryPath = directoryPath;
        else
            this.directoryPath = directoryPath + "/";

        this.directoryName = directory.getName();
        this.subDirectory = new LinkedList<>();
        this.files = new LinkedList<>();
        // 每当初初始化一个目录实体的时候，会自动去扫描该目录下的全部文件
        this.iterateDirectory(root);
    }

    public void iterateDirectory(TrieTree root) {
        File directory = new File(directoryPath);
        String[] sub = directory.list();
        if (sub != null)
            for (String s : sub) {
                File subFile = new File(directoryPath + s);
                if (subFile.isDirectory()) {
                    DirectoryEntity directoryEntity = new DirectoryEntity(directoryPath + s, root);
                    subDirectory.add(directoryEntity);
                } else {
                    files.add(s);
                    root.insert(s, directoryPath + s);
                }
            }
    }
}
