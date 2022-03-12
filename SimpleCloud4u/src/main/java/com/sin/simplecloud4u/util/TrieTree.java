package com.sin.simplecloud4u.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// 字典树的实现
// 其主要目的是实现文件名的搜索
public class TrieTree {
    private class TrieNode {
        public boolean isEnd;
        public Map<Character, TrieNode> nextMap;
        public String fileName;
        public List<String> filePath;
        public int nextMaxLength;

        public TrieNode() {
            isEnd = false;
            nextMap = new HashMap<>();
            filePath = new LinkedList<>();
            nextMaxLength = 0;
        }
    }

    private TrieNode root;

    public TrieTree() {
        root = new TrieNode();
    }

    public void insert(String fileName, String filePath) {
        TrieNode temp = root;
        char[] ch = fileName.toCharArray();
        root.nextMaxLength = Math.max(root.nextMaxLength, ch.length);
        for (int i = 0; i < ch.length; i++) {
            if (!temp.nextMap.containsKey(ch[i])) {
                TrieNode node = new TrieNode();
                temp.nextMap.put(ch[i], node);
            }
            temp = temp.nextMap.get(ch[i]);
            temp.nextMaxLength = Math.max(temp.nextMaxLength, (ch.length - i - 1));
        }
        temp.isEnd = true;
        temp.fileName = fileName;
        temp.filePath.add(filePath);
    }

    public boolean delete(String fileName, String filePath) {
        TrieNode temp = root;
        for (char c : fileName.toCharArray()) {
            if (!temp.nextMap.containsKey(c))
                return false;
            temp = temp.nextMap.get(c);
        }
        if (!temp.isEnd)
            return false;
        temp.filePath.remove(filePath);
        return true;
    }

    public List<String> query(String fileName) {
        List<String> ret = new LinkedList<>();
        subQuery(fileName, 0, root, ret);
        return ret;
    }

    // 主要目标是找出全部含有该的数据列表
    public void subQuery(String str, int pos, TrieNode root, List<String> ret) {
        if (root == null)
            return;

        int len = str.length();

        if (len == pos) {
            if (root.isEnd)
                ret.addAll(root.filePath);
            for (TrieNode node : root.nextMap.values()) {
                if (node != null)
                    subQuery(str, len, node, ret);
            }
            return;
        }

        // 下面的条件都是需要我们去判断的
        // 如果长度比节点的最长后缀长度还要长的话，直接退出不用查找
        if ((len - pos - 1) > root.nextMaxLength)
            return;

        // 如果含有当前的下标,就直接去检索
        if (root.nextMap.containsKey(str.charAt(pos))) {
            subQuery(str, pos + 1, root.nextMap.get(str.charAt(pos)), ret);
        }

        // 如果当前在的位置包含了字符串下标为0
        if (root.nextMaxLength >= len && pos != 0 && root.nextMap.containsKey(str.charAt(0))) {
            subQuery(str, 1, root.nextMap.get(str.charAt(0)), ret);
        }

        // 丢给下面的遍历
        if (root.nextMaxLength > len) {
            for (TrieNode node : root.nextMap.values()) {
                if (node != null)
                    subQuery(str, 0, node, ret);
            }
        }
    }
}