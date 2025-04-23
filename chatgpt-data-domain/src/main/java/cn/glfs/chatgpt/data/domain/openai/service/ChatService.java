package cn.glfs.chatgpt.data.domain.openai.service;

import cn.glfs.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.glfs.chatgpt.data.domain.openai.model.valobj.LogicCheckTypeVO;
import cn.glfs.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.glfs.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import cn.glfs.chatgpt.data.domain.openai.service.channel.impl.ChatGLMService;
import cn.glfs.chatgpt.data.domain.openai.service.channel.impl.ChatGPTService;
import cn.glfs.chatgpt.data.domain.openai.service.channel.impl.DeepSeekService;
import cn.glfs.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import cn.glfs.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.glfs.chatgpt.common.Constants;
import com.glfs.chatgpt.domain.chatModel.ChatChoice;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionRequest;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionResponse;
import com.glfs.chatgpt.domain.chatModel.Message;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
@Slf4j
public class ChatService extends AbstractChatService{

    public ChatService(ChatGPTService chatGPTService, ChatGLMService chatGLMService, DeepSeekService deepSeekService) {
        super(chatGPTService, chatGLMService, deepSeekService);
    }

    @Resource
    private DefaultLogicFactory logicFactory;
    @Override
    protected RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, UserAccountQuotaEntity userAccountQuotaEntity, String... logics) throws Exception {
        Map<String, ILogicFilter> logicFilterMap = logicFactory.openLogicFilter();//得到所有过滤器
        RuleLogicEntity<ChatProcessAggregate> entity = null;
        for (String code : logics) {
            if (DefaultLogicFactory.LogicModel.NULL.getCode().equals(code)) continue;
            entity = logicFilterMap.get(code).filter(chatProcess,userAccountQuotaEntity);
            if(!LogicCheckTypeVO.SUCCESS.equals(entity.getType())) return entity;//有一个过滤失败就返回，也就是白名单
        }
        return entity != null ? entity : RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS)
                .data(chatProcess)
                .build();
    }

}
