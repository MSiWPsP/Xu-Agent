package com.xucheng.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;


@SpringBootTest
class PgVectorVectorStoreConfigTest {

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Test
    void pgVectorVectorStore() {
        List<Document> documents = loveAppDocumentLoader.loadMarkDown();

        // 添加文档
        pgVectorVectorStore.add(documents);

        // 相似度查询
        List<Document> results = pgVectorVectorStore.similaritySearch(
                SearchRequest.builder()
                        .query("婚后关系不好")
                        .topK(5)
                        .build()
        );

        Assertions.assertNotNull(results);

    }

    @Test
    void testRetriever() {

        Filter.Expression expression =
                new FilterExpressionBuilder()
                        .eq("status", "已婚")
                        .build();

        DocumentRetriever retriever =
                VectorStoreDocumentRetriever.builder()
                        .vectorStore(pgVectorVectorStore)
                        .filterExpression(expression)
                        .similarityThreshold(0.0)
                        .topK(5)
                        .build();

        List<Document> results =
                retriever.retrieve(
                        new Query("我已经结婚了，但婚后关系不太亲密，怎么办")
                );

        System.out.println("检索数量：" + results.size());

        results.forEach(document -> {
            System.out.println("===========");
            System.out.println(document.getText());
            System.out.println(document.getMetadata());
            System.out.println(document.getScore());
        });

        Assertions.assertFalse(results.isEmpty());
    }

    @Test
    void testNoFilter() {

        DocumentRetriever retriever =
                VectorStoreDocumentRetriever.builder()
                        .vectorStore(pgVectorVectorStore)
                        .similarityThreshold(0.0)
                        .topK(5)
                        .build();

        List<Document> results =
                retriever.retrieve(new Query("婚后关系不好"));

        System.out.println("检索数量：" + results.size());

        results.forEach(document -> {
            System.out.println(document.getText());
            System.out.println(document.getMetadata());
            System.out.println(document.getScore());
        });
    }

    @Test
    void pgVectorVectorStoreLoad() {

        List<Document> documents =
                loveAppDocumentLoader.loadMarkDown();

        documents.forEach(document -> {
            System.out.println(document.getMetadata());
        });

        pgVectorVectorStore.add(documents);
    }
}