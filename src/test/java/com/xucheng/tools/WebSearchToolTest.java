package com.xucheng.tools;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebSearchToolTest {

    @Value("${search-api.api-key}")
    private String apikey;

    @Test
    void searchWeb() {
        WebSearchTool webSearchTool = new WebSearchTool(apikey);
        String s = webSearchTool.searchWeb("程序员鱼皮");
        System.out.println(s);
    }
}