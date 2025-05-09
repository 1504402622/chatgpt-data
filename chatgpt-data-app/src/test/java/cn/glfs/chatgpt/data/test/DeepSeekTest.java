package cn.glfs.chatgpt.data.test;


import cn.glfs.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.glfs.chatgpt.data.domain.openai.model.entity.MessageEntity;
import cn.glfs.chatgpt.data.domain.openai.service.IChatService;
import cn.glfs.chatgpt.data.types.exception.ChatGPTException;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.glfs.deepseek.common.Constants;
import com.glfs.deepseek.domain.chatModel.ChatChoice;
import com.glfs.deepseek.domain.chatModel.ChatCompletionRequest;
import com.glfs.deepseek.domain.chatModel.ChatCompletionResponse;
import com.glfs.deepseek.domain.chatModel.Message;
import com.glfs.deepseek.session.OpenAiSession;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Slf4j
@RunWith(SpringRunner.class)//JUnit 使用 Spring 提供的测试运行器来运行测试。
@SpringBootTest
public class DeepSeekTest {

    @Resource
    private OpenAiSession openAiSession;

    @Resource
    private IChatService chatService;

    @Test
    public void test() throws InterruptedException {
        // 2.构建异步响应对象（对Token过期拦截）
        // 如果验证失败，则通过 emitter 发送一个 TOKEN_ERROR 的响应码，并立即完成响应。
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);

        ChatCompletionRequest request= ChatCompletionRequest
                .builder()
                //新建列表：返回一个包含该参数作为唯一元素的不可变列表
                .messages(Collections.singletonList(Message.builder().role(Constants.Role.USER).content("写一个java冒泡排序").build()))
                .model(ChatCompletionRequest.Model.DeepSeek_V3.getCode())
                .build();


        ChatCompletionResponse completions = openAiSession.completions(request);
        log.info("测试结果:{}",completions);


    }

    @Test
    public void test_emitter() throws InterruptedException, JsonProcessingException {
        // 2.构建异步响应对象（对Token过期拦截）
        // 如果验证失败，则通过 emitter 发送一个 TOKEN_ERROR 的响应码，并立即完成响应。
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);

        ChatCompletionRequest request= ChatCompletionRequest
                .builder()
                .stream(true)
                //新建列表：返回一个包含该参数作为唯一元素的不可变列表
                .messages(Collections.singletonList(Message.builder().role(Constants.Role.USER).content("写一个java冒泡排序").build()))
                .model(ChatCompletionRequest.Model.DeepSeek_V3.getCode())
                .build();



        openAiSession.chatCompletions(request, new EventSourceListener() {
            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
                JsonArray choicesArray = jsonObject.getAsJsonArray("choices");

                if (choicesArray != null) {
                    Iterator<JsonElement> iterator = choicesArray.iterator();
                    while (iterator.hasNext()) {
                        JsonObject choiceObject = iterator.next().getAsJsonObject();
                        JsonObject deltaObject = choiceObject.getAsJsonObject("delta");
                        // 这里假设Constants.Role.ASSISTANT.getCode()在这个场景下逻辑不适用，暂不处理
                        // 应答完成
                        JsonElement finishReasonElement = choiceObject.get("finishReason");
                        if (finishReasonElement != null && "stop".equals(finishReasonElement.getAsString())) {
                            emitter.complete();  // 这里假设emitter是已定义的相关对象，根据实际情况调用
                            break;
                        }

                        // 发送信息
                        if (deltaObject != null) {
                            JsonElement contentElement = deltaObject.get("content");
                            if (contentElement != null) {
                                try {
                                    emitter.send(contentElement.getAsString());  // 这里假设emitter是已定义的相关对象，根据实际情况调用
                                } catch (Exception e) {
                                    throw new ChatGPTException(e.getMessage());  // 这里假设ChatGPTException是已定义的异常类，根据实际情况抛出
                                }
                            }
                        }
                    }
                }
            }
    });
        new CountDownLatch(1).await();

//                ChatCompletionResponse chatCompletionResponse = JSON.parseObject(data, ChatCompletionResponse.class);
//                List<ChatChoice> choices = chatCompletionResponse.getChoices();
//                for (ChatChoice chatChoice : choices) {
//                    Message delta = chatChoice.getMessage();
//                    if (Constants.Role.ASSISTANT.getCode().equals(delta.getRole())) continue;
//
//                    // 应答完成
//                    String finishReason = chatChoice.getFinishReason();
//                    if (StringUtils.isNoneBlank(finishReason) && "stop".equals(finishReason)) {
//                        emitter.complete();
//                        break;
//                    }
//
//                    // 发送信息
//                    try {
//                        emitter.send(delta.getContent());
//                    } catch (Exception e) {
//                        throw new ChatGPTException(e.getMessage());
//                    }
//                }
//
//            }
//        });
//        new CountDownLatch(1).await();
    }
}
