package com.xucheng.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 恋爱大师应用文档加载器
 */
@Component
@Slf4j
public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver){
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Document> loadMarkDown() {

        List<Document> allDocuments = new ArrayList<>();

        try {
            Resource[] resources =
                    resourcePatternResolver.getResources("classpath:document/*");

            for (Resource resource : resources) {

                String filename = resource.getFilename();

                String status = null;

                if (filename.contains("单身篇")) {
                    status = "单身";
                } else if (filename.contains("已婚篇")) {
                    status = "已婚";
                } else if (filename.contains("恋爱篇")) {
                    status = "恋爱";
                }

                MarkdownDocumentReaderConfig config =
                        MarkdownDocumentReaderConfig.builder()
                                .withHorizontalRuleCreateDocument(true)
                                .withIncludeCodeBlock(false)
                                .withIncludeBlockquote(false)
                                .withAdditionalMetadata("filename", filename)
                                .withAdditionalMetadata("status", status)
                                .build();

                MarkdownDocumentReader reader =
                        new MarkdownDocumentReader(resource, config);

                List<Document> documents = reader.get();

                allDocuments.addAll(documents);
            }

        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
        }

        return allDocuments;
    }

}
