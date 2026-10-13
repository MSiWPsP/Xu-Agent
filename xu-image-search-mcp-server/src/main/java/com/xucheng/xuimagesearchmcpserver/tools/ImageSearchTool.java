package com.xucheng.xuimagesearchmcpserver.tools;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Component
public class ImageSearchTool {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${pixabay.api-key}")
    private String apiKey;

    public ImageSearchTool(RestClient.Builder restClientBuilder,
                           ObjectMapper objectMapper) {
        this.restClient = restClientBuilder
                .baseUrl("https://pixabay.com")
                .build();
        this.objectMapper = objectMapper;
    }

    @Tool(description = "Search images from Pixabay according to the given keyword")
    public String searchImage(
            @ToolParam(description = "Image search keyword")
            String query) {

        if (query == null || query.isBlank()) {
            return """
                    {
                      "error": "Search query cannot be empty"
                    }
                    """;
        }

        // 创建新变量，不修改 query
        final String searchQuery =
                query.length() > 100
                        ? query.substring(0, 100)
                        : query;

        try {
            PixabayResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/")
                            .queryParam("key", apiKey)
                            .queryParam("q", searchQuery)
                            .queryParam("image_type", "photo")
                            .queryParam("safesearch", true)
                            .queryParam("order", "popular")
                            .queryParam("per_page", 5)
                            .build())
                    .retrieve()
                    .body(PixabayResponse.class);

            if (response == null
                    || response.hits() == null
                    || response.hits().isEmpty()) {

                return """
                        {
                          "total": 0,
                          "images": []
                        }
                        """;
            }

            List<ImageResult> images = response.hits()
                    .stream()
                    .map(hit -> new ImageResult(
                            hit.id(),
                            hit.tags(),
                            hit.pageURL(),
                            hit.previewURL(),
                            hit.webformatURL(),
                            hit.largeImageURL(),
                            hit.imageWidth(),
                            hit.imageHeight(),
                            hit.user()
                    ))
                    .toList();

            ImageSearchResult result =
                    new ImageSearchResult(response.totalHits(), images);

            return objectMapper.writeValueAsString(result);

        } catch (RestClientResponseException e) {

            return """
                    {
                      "error": "Pixabay API request failed",
                      "status": %d
                    }
                    """.formatted(e.getStatusCode().value());

        } catch (JsonProcessingException e) {

            return """
                    {
                      "error": "JSON serialization failed"
                    }
                    """;

        } catch (Exception e) {

            return """
                    {
                      "error": "Image search failed"
                    }
                    """;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PixabayResponse(
            Integer total,
            Integer totalHits,
            List<PixabayHit> hits
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PixabayHit(
            Long id,
            String pageURL,
            String type,
            String tags,
            String previewURL,
            Integer previewWidth,
            Integer previewHeight,
            String webformatURL,
            Integer webformatWidth,
            Integer webformatHeight,
            String largeImageURL,
            Integer imageWidth,
            Integer imageHeight,
            Long imageSize,
            Long views,
            Long downloads,
            Long likes,
            Long comments,
            Long user_id,
            String user,
            String userImageURL
    ) {
    }

    public record ImageResult(
            Long id,
            String tags,
            String sourcePage,
            String previewUrl,
            String imageUrl,
            String largeImageUrl,
            Integer width,
            Integer height,
            String author
    ) {
    }

    public record ImageSearchResult(
            Integer total,
            List<ImageResult> images
    ) {
    }
}