package com.xucheng.xuimagesearchmcpserver.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ImageSearchToolTest {

    @Resource
    private ImageSearchTool imageSearchTool;

    @Test
    void SearchImage(){
        String result = imageSearchTool.searchImage("computer");
        Assertions.assertNotNull(result);
    }
}
