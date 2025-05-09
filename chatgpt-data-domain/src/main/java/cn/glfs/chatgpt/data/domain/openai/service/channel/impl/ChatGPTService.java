package cn.glfs.chatgpt.data.domain.openai.service.channel.impl;

import cn.glfs.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.glfs.chatgpt.data.domain.openai.service.channel.OpenAiGroupService;
import cn.glfs.chatgpt.data.types.enums.ChatGPTModel;
import cn.glfs.chatgpt.data.types.exception.ChatGPTException;
import com.alibaba.fastjson.JSON;
import com.glfs.chatgpt.session.OpenAiSession;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import com.glfs.chatgpt.common.Constants;
import com.glfs.chatgpt.domain.chatModel.ChatChoice;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionRequest;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionResponse;
import com.glfs.chatgpt.domain.chatModel.Message;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatGPTService implements OpenAiGroupService {

    @Autowired(required = false)
    protected OpenAiSession chatGPTOpenAiSession;

    @Override
    public void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws Exception {
        if (null == chatGPTOpenAiSession) {
            emitter.send("ChatGPT 通道，模型调用未开启，可以选择其他模型对话！");
            return;
        }

        // 1. 请求消息
        List<Message> messages = chatProcess.getMessages().stream()
                .map(entity -> Message.builder()
                        .role(Constants.Role.valueOf(entity.getRole().toUpperCase()))
                        .content(entity.getContent())
                        .name(entity.getName())
                        .build())
                .collect(Collectors.toList());

        // 2. 封装参数
        ChatCompletionRequest chatCompletion = ChatCompletionRequest
                .builder()
                .stream(true)
                .messages(messages)
                .model(ChatGPTModel.GPT_3_5_TURBO.getCode())
                .build();

        // 3.2 请求应答
        chatGPTOpenAiSession.chatCompletions(chatCompletion, new EventSourceListener() {
            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                ChatCompletionResponse chatCompletionResponse = JSON.parseObject(data, ChatCompletionResponse.class);
                List<ChatChoice> choices = chatCompletionResponse.getChoices();
                for (ChatChoice chatChoice : choices) {
                    Message delta = chatChoice.getDelta();
                    if (Constants.Role.ASSISTANT.getCode().equals(delta.getRole())) continue;

                    // 应答完成
                    String finishReason = chatChoice.getFinishReason();
                    if (StringUtils.isNoneBlank(finishReason) && "stop".equals(finishReason)) {
                        emitter.complete();
                        break;
                    }

                    // 发送信息
                    try {
                        emitter.send(delta.getContent());
                    } catch (Exception e) {
                        throw new ChatGPTException(e.getMessage());
                    }
                }

            }
        });
    }
}
