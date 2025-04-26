package cn.glfs.chatgpt.data.domain.openai.service.channel.impl;



import cn.glfs.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.glfs.chatgpt.data.domain.openai.service.channel.OpenAiGroupService;
import cn.glfs.chatgpt.data.types.exception.ChatGPTException;
import com.alibaba.fastjson.JSON;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DeepSeekService implements OpenAiGroupService {

    @Autowired(required = false)
    protected OpenAiSession deepSeekOpenAiSession;

    @Override
    public void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws Exception {
        if (null == deepSeekOpenAiSession) {
            emitter.send("DeepSeek 通道，模型调用未开启，可以选择其他模型对话！");
            return;
        }


        // 1. 请求消息
        List<Message> messages = chatProcess.getMessages().stream()
                .map(entity -> Message.builder()
                        .role(Constants.Role.USER)
                        .content(entity.getContent())
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
                        if (StringUtils.isNoneBlank(finishReason) && "stop".equals(finishReason)) {
                            emitter.complete();
                        }
                        emitter.send(content);
                    }
                }catch (IOException e) {
                        e.printStackTrace();
                }
            }
        });
    }
}
