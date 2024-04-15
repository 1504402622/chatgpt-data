package cn.glfs.chatgpt.data.domain.openai.service.rule;

import cn.glfs.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.glfs.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;

/**
 * 规则过滤接口
 */
public interface ILogicFilter<T> {

   RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, T data) throws Exception;

}

