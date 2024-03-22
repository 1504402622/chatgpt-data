package cn.glfs.chatgpt.data.domain.weixin.service.message;

import cn.glfs.chatgpt.data.domain.weixin.model.entity.MessageTextEntity;
import cn.glfs.chatgpt.data.domain.weixin.model.entity.UserBehaviorMessageEntity;
import cn.glfs.chatgpt.data.domain.weixin.model.valobj.MsgTypeVO;
import cn.glfs.chatgpt.data.domain.weixin.service.IWeiXinBehaviorService;
import cn.glfs.chatgpt.data.types.exception.ChatGPTException;
import cn.glfs.chatgpt.data.types.sdk.weixin.XmlUtil;
import com.google.common.cache.Cache;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 受理消息服务
 */
@Service
public class WeiXinBehaviorService implements IWeiXinBehaviorService {
    @Value("${wx.config.originalid}")
    private String originalId;

    @Resource
    private Cache<String, String> codeCache;

    /**
     * 1. 用户的请求行文，分为事件event、消息text，这里我们只处理消息内容
     * 2. 用户行为、消息类型，是多样性的，这部分如果用户有更多的扩展需求，可以使用设计模式【模板模式 + 策略模式 + 工厂模式】，分拆逻辑。
     */

    /**
     * 如果是文本，接收事件并存储验证码到缓存
     * @param userBehaviorMessageEntity
     * @return
     */
    @Override
    public String acceptUserBehavior(UserBehaviorMessageEntity userBehaviorMessageEntity) {
        // Event 事件类型，忽略处理(如果以后再加一些其他需要事件的功能就可以进行拓展)
        if (MsgTypeVO.EVENT.getCode().equals(userBehaviorMessageEntity.getMsgType())) {
            return "";
        }

        //文本类型
        if(MsgTypeVO.TEXT.getCode().equals(userBehaviorMessageEntity.getMsgType())){
            //缓存验证码
            String isExistCode = codeCache.getIfPresent(userBehaviorMessageEntity.getOpenId());

            //判断验证码 - 不考虑验证码重复问题(如果缓存中有这个验证码就不用去再放了,就是用户可能多点)
            if(StringUtils.isBlank(isExistCode)){
                //创建验证码（双向绑定键值对）
                String code = RandomStringUtils.randomNumeric(4);
                codeCache.put(code,userBehaviorMessageEntity.getOpenId());
                codeCache.put(userBehaviorMessageEntity.getOpenId(),code);
                isExistCode = code;
            }

            //反馈文本
            MessageTextEntity res = new MessageTextEntity();
            res.setToUserName(userBehaviorMessageEntity.getOpenId());
            res.setFromUserName(originalId);
            res.setCreateTime(String.valueOf(System.currentTimeMillis()));
            res.setMsgType("text");
            res.setContent(String.format("您的验证码为：%s 有效期%d分钟！", isExistCode, 3));
            return XmlUtil.beanToXml(res);
        }
        throw new ChatGPTException(userBehaviorMessageEntity.getMsgType() + " 未被处理的行为类型 Err！");

    }
}








