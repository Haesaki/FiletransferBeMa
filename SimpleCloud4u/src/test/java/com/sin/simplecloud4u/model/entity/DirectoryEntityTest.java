package com.sin.simplecloud4u.model.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DirectoryEntityTest {
    @Test
    public void directoryFindTest() {
        String directoryPath = "C:\\Users\\laoha\\IdeaProjects\\filetransferbema\\SimpleCloud4u";
        DirectoryEntity directory = new DirectoryEntity(directoryPath, true);
        iteratorOutputFileTree(directory, 0);
    }

    public void iteratorOutputFileTree(DirectoryEntity directoryEntity, int cnt) {
        if (directoryEntity.getSubDirectory() != null)
            for (DirectoryEntity subDirectory : directoryEntity.getSubDirectory()) {
                iteratorOutputFileTree(subDirectory, cnt + 1);
            }
        String prefix = " ".repeat(cnt);
        if (directoryEntity.getFiles() != null)
            for (String s : directoryEntity.getFiles()) {
                System.out.println(prefix + s);
            }
    }
}
