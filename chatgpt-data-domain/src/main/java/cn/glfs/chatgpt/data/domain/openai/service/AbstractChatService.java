package cn.glfs.chatgpt.data.domain.openai.service;

import cn.glfs.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.glfs.chatgpt.data.types.common.Constants;
import cn.glfs.chatgpt.data.types.exception.ChatGPTException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.glfs.chatgpt.session.OpenAiSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
@Slf4j
public abstract class AbstractChatService implements IChatService{
    @Resource
    protected OpenAiSession openAiSession;
    @Override
    public ResponseBodyEmitter completions(ChatProcessAggregate chatProcess) {
        //1.校验权限
        if(!"6666".equals(chatProcess.getToken())){
            throw new ChatGPTException(Constants.ResponseCode.TOKEN_ERROR.getCode(), Constants.ResponseCode.TOKEN_ERROR.getInfo());
        }

        //2.请求应答
        //设置超时时间
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(3*60*1000L);
        emitter.onCompletion(() -> {
            log.info("流式问答请求完成，使用模型：{}", chatProcess.getModel());
        });

        emitter.onError(throwable -> log.error("流式问答请求疫情，使用模型：{}", chatProcess.getModel(), throwable));

        // 3. 应答处理
        try {
            this.doMessageResponse(chatProcess, emitter);
        } catch (Exception e) {
            throw new ChatGPTException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }

        //返回响应结果
        return emitter;
    }

    //对消息的实际操作
    protected abstract void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter responseBodyEmitter) throws JsonProcessingException;
}