package cn.glfs.chatgpt.data.domain.openai.service;

import cn.glfs.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.glfs.chatgpt.data.domain.openai.model.valobj.LogicCheckTypeVO;
import cn.glfs.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.glfs.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import cn.glfs.chatgpt.data.domain.openai.repository.IOpenAiRepository;
import cn.glfs.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
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
    @Resource
    private IOpenAiRepository openAiRepository;
    @Override
    public ResponseBodyEmitter completions(ResponseBodyEmitter emitter,ChatProcessAggregate chatProcess) {

        try {
            // 1. 请求应答
            emitter.onCompletion(() -> {
                log.info("流式问答请求完成，使用模型：{}", chatProcess.getModel());
            });
            emitter.onError(throwable -> log.error("流式问答请求疫情，使用模型：{}", chatProcess.getModel(), throwable));

            // 2. 账户获取
            UserAccountQuotaEntity userAccountQuotaEntity = openAiRepository.queryUserAccount(chatProcess.getOpenid());

            // 3. 规则过滤
            RuleLogicEntity<ChatProcessAggregate> ruleLogicEntity = this.doCheckLogic(chatProcess, userAccountQuotaEntity,
                    DefaultLogicFactory.LogicModel.ACCESS_LIMIT.getCode(),
                    DefaultLogicFactory.LogicModel.SENSITIVE_WORD.getCode(),
                    null != userAccountQuotaEntity ? DefaultLogicFactory.LogicModel.ACCOUNT_STATUS.getCode() : DefaultLogicFactory.LogicModel.NULL.getCode(),
                    null != userAccountQuotaEntity ? DefaultLogicFactory.LogicModel.MODEL_TYPE.getCode() : DefaultLogicFactory.LogicModel.NULL.getCode(),
                    null != userAccountQuotaEntity ? DefaultLogicFactory.LogicModel.USER_QUOTA.getCode() : DefaultLogicFactory.LogicModel.NULL.getCode()
            );

            if (!LogicCheckTypeVO.SUCCESS.equals(ruleLogicEntity.getType())){
                emitter.send(ruleLogicEntity.getInfo());
                emitter.complete();
                return emitter;
            }

            // 3.应答处理
            this.doMessageResponse(chatProcess, emitter);
        } catch (Exception e) {
            throw new ChatGPTException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }

        // 4.返回响应结果
        return emitter;
    }
    //规则过滤实际操作
    protected abstract RuleLogicEntity<ChatProcessAggregate> doCheckLogic(ChatProcessAggregate chatProcess, UserAccountQuotaEntity userAccountQuotaEntity, String... logics) throws Exception;

    //对消息的实际操作
    protected abstract void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter responseBodyEmitter) throws JsonProcessingException;
}
