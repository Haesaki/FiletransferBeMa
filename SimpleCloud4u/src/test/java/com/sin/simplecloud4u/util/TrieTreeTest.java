package com.sin.simplecloud4u.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class TrieTreeTest {
    @Test
    public void testInsertAndSelect() throws IOException {
        String[] strs = new String[]{"这是.jpg",
                "这是什.jpg",
                "这是什么.jpg",
                "这是什么东.jpg",
                "这是什么东西.jpg",
                "哈哈.jpg",
                "哈哈哈.jpg",
                "哈哈哈哈.jpg",
                "哈哈哈哈哈.jpg"};
        String queryWord = "这";

        TrieTree tree = new TrieTree();

        for (String s : strs) {
            tree.insert(s, "/src/" + s);
        }
        List<String> ret = tree.query(queryWord);
//        System.out.println(ret);
        Assertions.assertEquals(ret.size(), 5);

        ret = tree.query("什么");
        System.out.println(ret);
        Assertions.assertEquals(ret.size(), 3);
    }
}
