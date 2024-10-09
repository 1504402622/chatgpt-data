package cn.glfs.chatgpt.data.domain.openai.service.rule.impl;

import cn.glfs.chatgpt.data.domain.openai.annotation.LogicStrategy;
import cn.glfs.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.glfs.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import cn.glfs.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.glfs.chatgpt.data.domain.openai.model.valobj.LogicCheckTypeVO;
import cn.glfs.chatgpt.data.domain.openai.model.valobj.UserAccountStatusVO;
import cn.glfs.chatgpt.data.domain.openai.repository.IOpenAiRepository;
import cn.glfs.chatgpt.data.domain.openai.service.IChatService;
import cn.glfs.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import cn.glfs.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;

/**
 * 账户校验
 */

@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.ACCOUNT_STATUS)
public class AccountStatusFilter implements ILogicFilter<UserAccountQuotaEntity> {


    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data) throws Exception {
        // 账户可用，直接放行
        if (UserAccountStatusVO.AVAILABLE.equals(data.getUserAccountStatusVO())) {
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
        }

        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .info("您的账户已冻结，暂时不可使用。如果有疑问，可以联系客户解冻账户。")
                .type(LogicCheckTypeVO.REFUSE).data(chatProcess).build();
    }
}
