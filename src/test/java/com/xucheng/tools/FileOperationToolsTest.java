package com.xucheng.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationToolsTest {

    @Test
    void readFile() {
        FileOperationTool fileOperationTools = new FileOperationTool();
        String fileName = "许城.txt";
        String result = fileOperationTools.readFile(fileName);
        assertNotNull(result);
    }

    @Test
    void writeFile() {
        FileOperationTool fileOperationTools = new FileOperationTool();
        String fileName = "许城.txt";
        String content = "我是许城";
        String result = fileOperationTools.writeFile(fileName,content);
        assertNotNull(result);
    }
}