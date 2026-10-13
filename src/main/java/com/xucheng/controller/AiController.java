package com.xucheng.controller;

import com.xucheng.agent.XuManus;
import com.xucheng.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;


    /**
     * 同步调用ai恋爱大师应用
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message,String chatId){
        return loveApp.doChat(message,chatId);
    }

    /**
     * SSE流式调用ai恋爱大师应用
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/sse",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId){
        return loveApp.doChatByStream(message,chatId);
    }

    /**
     * SSE流式调用ai恋爱大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId){
        return loveApp.doChatByStream(message,chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * SSE流式调用ai恋爱大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/sse_emmiter")
    public SseEmitter doChatWithLoveAppServerSseEmitter(String message, String chatId){
        SseEmitter sseEmitter = new SseEmitter(180000L);//三分钟超时
        loveApp.doChatByStream(message,chatId)
                .subscribe(chunk ->{
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                },sseEmitter::completeWithError,sseEmitter::complete);
        return sseEmitter;
    }

    /**
     * 流式调用Manus超级智能体
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message){
        XuManus xuManus = new XuManus(allTools,dashscopeChatModel);
        return xuManus.runStream(message);
    }
}
