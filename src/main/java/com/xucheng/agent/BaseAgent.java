package com.xucheng.agent;

import com.alibaba.cloud.nacos.utils.StringUtils;
import com.xucheng.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Data
@Slf4j
public abstract class BaseAgent {

    private String name;

    private String systemPrompt;

    private String nextStepPrompt;

    private AgentState state = AgentState.IDLE;

    private int currentStep = 0;

    private int maxStep = 10;

    private ChatClient chatClient;

    private List<Message> messageList = new ArrayList<>();

    public String run(String userPrompt) {

        // 1. 基础校验
        if (state != AgentState.IDLE) {
            throw new RuntimeException(
                    "Cannot run agent from state: " + state
            );
        }

        if (StringUtils.isBlank(userPrompt)) {
            throw new RuntimeException(
                    "Cannot run agent with blank user prompt"
            );
        }

        // 2. 设置运行状态
        this.state = AgentState.RUNNING;

        // 记录用户消息
        messageList.add(new UserMessage(userPrompt));

        List<String> results = new ArrayList<>();

        try {

            for (int i = 0;
                 i < maxStep && state == AgentState.RUNNING;
                 i++) {

                currentStep = i + 1;

                log.info(
                        "Executing step {}/{}",
                        currentStep,
                        maxStep
                );

                String stepResult = step();

                results.add(
                        "Step " + currentStep + ": " + stepResult
                );
            }

            // 达到最大步骤数，但 Agent 仍然没有主动结束
            if (state == AgentState.RUNNING) {

                state = AgentState.FINISHED;

                results.add(
                        "Terminated: Reached maximum number of steps: "
                                + maxStep
                );

                log.warn(
                        "Agent reached maximum number of steps: {}",
                        maxStep
                );
            }

        } catch (Exception e) {

            state = AgentState.ERROR;

            log.error(
                    "Error during agent execution",
                    e
            );

            results.add(
                    "执行错误: " + e.getMessage()
            );

        } finally {

            cleanup();
        }

        return String.join("\n", results);
    }
    public SseEmitter runStream(String userPrompt) {

        SseEmitter sseEmitter = new SseEmitter(300000L);//5分钟超时
        //使用线程异步处理,避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            // 1. 基础校验
            try {
                if (state != AgentState.IDLE) {
                    sseEmitter.send("Cannot run agent from state: " + state);
                    sseEmitter.complete();
                    return;
                }
                if (StringUtils.isBlank(userPrompt)) {
                    sseEmitter.send("Cannot run agent with blank user prompt");
                    sseEmitter.complete();
                    return;
                }
            }catch (Exception e){
                sseEmitter.completeWithError(e);
                return;
            }

            // 2. 设置运行状态
            this.state = AgentState.RUNNING;
            // 记录用户消息
            messageList.add(new UserMessage(userPrompt));
            List<String> results = new ArrayList<>();
            try {

                for (int i = 0;
                     i < maxStep && state == AgentState.RUNNING;
                     i++) {
                    currentStep = i + 1;
                    log.info(
                            "Executing step {}/{}",
                            currentStep,
                            maxStep
                    );
                    String stepResult = step();
                    results.add(
                            "Step " + currentStep + ": " + stepResult
                    );
                    //输出当前每一步的结果到SSE
                    sseEmitter.send(stepResult);
                }
                // 达到最大步骤数，但 Agent 仍然没有主动结束
                if (state == AgentState.RUNNING) {
                    state = AgentState.FINISHED;
                    results.add(
                            "Terminated: Reached maximum number of steps: "
                                    + maxStep
                    );
                    sseEmitter.send("执行结束,达到最大步骤(" + maxStep + ")");
                    log.warn(
                            "Agent reached maximum number of steps: {}",
                            maxStep
                    );
                }
                //正常完成
                sseEmitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error(
                        "Error during agent execution",
                        e
                );
                try {
                    sseEmitter.send("执行错误: " + e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
                results.add(
                        "执行错误: " + e.getMessage()
                );
            } finally {
                cleanup();
            }
        });

        //设置超时回调
        sseEmitter.onTimeout(() ->{
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timeout");
        });
        //设置完成回调
        sseEmitter.onCompletion(() ->{
            if (this.state == AgentState.RUNNING){
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });

        return sseEmitter;
    }

    public abstract String step();

    protected void cleanup() {

    }
}