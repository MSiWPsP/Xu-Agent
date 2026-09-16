package com.xucheng.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppDocumentLoaderTest {

    @Autowired
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Test
    void loadMarkDown() {
        List<Document> documents = loveAppDocumentLoader.loadMarkDown();
        for (Document document : documents) {
            System.out.println(document);
        }
    }
}