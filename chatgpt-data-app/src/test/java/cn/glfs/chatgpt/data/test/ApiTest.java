package cn.glfs.chatgpt.data.test;



import cn.glfs.chatgpt.data.types.exception.ChatGPTException;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;


import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glfs.deepseek.common.Constants;
import com.glfs.deepseek.domain.chatModel.ChatChoice;
import com.glfs.deepseek.domain.chatModel.ChatCompletionRequest;
import com.glfs.deepseek.domain.chatModel.ChatCompletionResponse;
import com.glfs.deepseek.domain.chatModel.Message;
import com.glfs.deepseek.session.OpenAiSession;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
@RunWith(SpringRunner.class)//JUnit 使用 Spring 提供的测试运行器来运行测试。
@SpringBootTest
public class ApiTest {

    @Resource
    private OpenAiSession openAiSession;




    /**
     * 此对话模型 3.5 接近于官网体验 & 流式应答
     */
    @Test
    public void test_chat_completions_stream() throws JsonProcessingException, InterruptedException {
        // 1. 创建参数
        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                .stream(true)
                .messages(Collections.singletonList(Message.builder().role(Constants.Role.USER).content("写一个java冒泡排序").build()))
                .model(ChatCompletionRequest.Model.DeepSeek_R1.getCode())
                .build();
        // 2. 发起一次异步的事件源（EventSource）请求
        EventSource eventSource = openAiSession.chatCompletions(chatCompletion, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {

                log.info("测试结果：{}", data);
            }
        });
        // 等待
        new CountDownLatch(1).await();
    }

    @Test
    public void test_chat_completions_stream2() throws JsonProcessingException, InterruptedException {
        // 1. 创建参数
        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                .stream(true)
                .messages(Collections.singletonList(Message.builder().role(Constants.Role.USER).content("写一个java冒泡排序").build()))
                .model(ChatCompletionRequest.Model.DeepSeek_R1.getCode())
                .build();
        // 2. 发起一次异步的事件源（EventSource）请求
        EventSource eventSource = openAiSession.chatCompletions(chatCompletion, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {

                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonNode rootNode = objectMapper.readTree(data);
                    JsonNode choicesNode = rootNode.get("choices");
                    if (choicesNode != null && choicesNode.isArray() && choicesNode.size() > 0) {
                        JsonNode firstChoice = choicesNode.get(0);
                        JsonNode deltaNode = firstChoice.get("delta");
                        String content = null;
                        if (deltaNode != null) {
                            JsonNode contentNode = deltaNode.get("content");
                            if (contentNode != null && !contentNode.isNull()) {
                                content = contentNode.asText();
                            } else {
                                JsonNode reasoningContentNode = deltaNode.get("reasoning_content");
                                if (reasoningContentNode != null && !reasoningContentNode.isNull()) {
                                    content = reasoningContentNode.asText();
                                }
                            }
                        }
                        String finishReason = firstChoice.get("finish_reason") != null ? firstChoice.get("finish_reason").asText() : null;

                        System.out.println("content: " + content);
                        System.out.println("finish_reason: " + finishReason);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
        // 等待
        new CountDownLatch(1).await();
    }

}
