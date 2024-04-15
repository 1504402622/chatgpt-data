package cn.glfs.chatgpt.data.domain.openai.service.rule.impl;

import cn.glfs.chatgpt.data.domain.openai.annotation.LogicStrategy;
import cn.glfs.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.glfs.chatgpt.data.domain.openai.model.valobj.LogicCheckTypeVO;
import cn.glfs.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.glfs.chatgpt.data.domain.openai.model.entity.RuleLogicEntity;
import cn.glfs.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import cn.glfs.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;
import com.google.common.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 访问次数过滤
 */
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.ACCESS_LIMIT)
public class AccessLimitFilter implements ILogicFilter<UserAccountQuotaEntity> {

    @Value("${app.config.limit-count:10}")
    private Integer limitCount;
    @Value("${app.config.white-list}")
    private String whiteListStr;
    @Resource
    private Cache<String, Integer> visitCache;
    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess,UserAccountQuotaEntity data) throws Exception {
        //1.白名单用户直接放行,利用isWhiteList方法查看该用户id是否在白名单中
        if(chatProcess.isWhiteList(whiteListStr)){
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
        }
        String openid = chatProcess.getOpenid();

        // 2. 个人账户不为空，不做系统访问次数拦截
        if (null != data) {
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
        }

        //2.访问次数判断
         int visitCount = visitCache.get(openid,()->0);//()->0 表示创建了一个匿名的 Supplier 对象，当需要默认值时会调用它的 get 方法来得到 0。
         if(visitCount <limitCount){
             visitCache.put(openid,visitCount+1);
             return RuleLogicEntity.<ChatProcessAggregate>builder()
                     .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
         }

        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .info("您今日的免费" + limitCount + "次，已耗尽！")
                .type(LogicCheckTypeVO.REFUSE)
                .data(chatProcess)
                .build();
    }
}
