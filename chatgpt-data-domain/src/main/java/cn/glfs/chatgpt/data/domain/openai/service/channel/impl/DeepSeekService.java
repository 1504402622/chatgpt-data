package cn.glfs.chatgpt.data.domain.openai.service.channel.impl;


import cn.glfs.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.glfs.chatgpt.data.domain.openai.service.channel.OpenAiGroupService;
import cn.glfs.chatgpt.data.types.exception.ChatGPTException;
import com.alibaba.fastjson.JSON;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DeepSeekService implements OpenAiGroupService {

    @Autowired(required = false)
    protected OpenAiSession deepSeekOpenAiSession;

    /**
     * 等待Chiose实体修改后启用
     * @param chatProcess
     * @param emitter
     * @throws Exception
     */
//    @Override
//    public void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws Exception {
//        if (null == deepSeekOpenAiSession) {
//            emitter.send("ChatGPT 通道，模型调用未开启，可以选择其他模型对话！");
//            return;
//        }
//
//
//        // 1. 请求消息
//        List<Message> messages = chatProcess.getMessages().stream()
//                .map(entity -> Message.builder()
//                        .role(Constants.Role.USER)
//                        .content(entity.getContent())
//                        .name(entity.getName())
//                        .build())
//                .collect(Collectors.toList());
//
//        // 2. 封装参数
//        ChatCompletionRequest chatCompletion = ChatCompletionRequest
//                .builder()
//                .stream(true)
//                .messages(messages)
//                .model(chatProcess.getModel())
//                .build();
//
//        // 3.2 请求应答
//        deepSeekOpenAiSession.chatCompletions(chatCompletion, new EventSourceListener() {
//            @Override
//            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
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
//    }



    @Override
    public void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws Exception {
        if (null == deepSeekOpenAiSession) {
            emitter.send("ChatGPT 通道，模型调用未开启，可以选择其他模型对话！");
            return;
        }


        // 1. 请求消息
        List<Message> messages = chatProcess.getMessages().stream()
                .map(entity -> Message.builder()
                        .role(Constants.Role.USER)
                        .content(entity.getContent())
                        .name(entity.getName())
                        .build())
                .collect(Collectors.toList());

        // 2. 封装参数
        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                .stream(true)
                .messages(messages)
                .model(chatProcess.getModel())
                .build();

        // 3.2 请求应答
        deepSeekOpenAiSession.chatCompletions(chatCompletion, new EventSourceListener() {
            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
                JsonArray choicesArray = jsonObject.getAsJsonArray("choices");

                if (choicesArray != null) {
                    Iterator<JsonElement> iterator = choicesArray.iterator();
                    while (iterator.hasNext()) {
                        JsonObject choiceObject = iterator.next().getAsJsonObject();
                        JsonObject deltaObject = choiceObject.getAsJsonObject("delta");
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
    }
}
