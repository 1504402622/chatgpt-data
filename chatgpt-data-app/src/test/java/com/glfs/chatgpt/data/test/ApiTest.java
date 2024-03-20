package com.glfs.chatgpt.data.test;



import cn.glfs.chatcpt.data.trigger.http.dto.ChatGPTRequestDTO;
import cn.glfs.chatcpt.data.trigger.http.dto.MessageEntity;
import cn.glfs.chatgpt.data.types.enums.ChatGPTModel;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.glfs.chatgpt.common.Constants;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionRequest;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionResponse;
import com.glfs.chatgpt.domain.chatModel.Message;
import com.glfs.chatgpt.session.OpenAiSession;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
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
     * 模拟3.5
     */
    @Test
    public void test_chat_completions(){
        //1.创建参数
        ChatCompletionRequest completionRequest=ChatCompletionRequest
                        .builder()
                        //新建列表：返回一个包含该参数作为唯一元素的不可变列表
                        .messages(Collections.singletonList(Message.builder().role(Constants.Role.USER).content("写一个java冒泡排序").build()))
                        .model(ChatCompletionRequest.Model.GPT_3_5_TURBO.getCode())
                        .build();
        //2.发送请求
        ChatCompletionResponse chatCompletionResponse = openAiSession.completions(completionRequest);
        //3.解析结果
        chatCompletionResponse.getChoices().forEach(e->{
            log.info("测试结果:{}",e.getMessage());
        });
    }

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
                .model(ChatCompletionRequest.Model.GPT_3_5_TURBO.getCode())
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
    public void test_request_json(){
        List<MessageEntity> messages = new ArrayList<>();

//        MessageEntity messageEntity00 = new MessageEntity();
//        messageEntity00.setRole(Constants.Role.USER.getCode());
//        messageEntity00.setContent("你是一个非常专业的 Java 开发工程师，具备世界一流水平，曾在多家世界级互联网公司任职，可以写出非常优秀易于扩展和迭代的代码。");

        MessageEntity messageEntity01 = new MessageEntity();
        messageEntity01.setRole(Constants.Role.USER.getCode());
        messageEntity01.setContent("写一个java冒泡排序");

//        messages.add(messageEntity00);
        messages.add(messageEntity01);

        ChatGPTRequestDTO requestDTO = ChatGPTRequestDTO.builder().model(ChatGPTModel.GPT_3_5_TURBO.getCode()).messages(messages).build();

        log.info(JSON.toJSONString(requestDTO));
        // {"messages":[{"content":"你是一个非常专业的 Java 开发工程师，具备世界一流水平，曾在多家世界级互联网公司任职，可以写出非常优秀易于扩展和迭代的代码。","role":"user"},{"content":"写一个java冒泡排序","role":"user"}],"model":"gpt-3.5-turbo"}

        String str = "{\"messages\":[{\"content\":\"写一个java冒泡排序\",\"role\":\"user\"}],\"model\":\"gpt-3.5-turbo\"}";

    }
}
