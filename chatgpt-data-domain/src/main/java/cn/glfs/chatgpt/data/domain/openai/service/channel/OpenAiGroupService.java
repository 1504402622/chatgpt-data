package cn.glfs.chatgpt.data.domain.openai.service.channel;


import cn.glfs.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;


public interface OpenAiGroupService {

    void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws Exception;

}
