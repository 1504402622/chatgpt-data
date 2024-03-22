package cn.glfs.chatgpt.data.domain.weixin.service.validate;

import cn.glfs.chatgpt.data.domain.weixin.service.IWeiXinValidateService;
import cn.glfs.chatgpt.data.types.sdk.weixin.SignatureUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 对接微信验签服务（需要在首次配置服务器时进行校验）
 */
@Service
public class WeiXinValidateService implements IWeiXinValidateService {

    @Value("${wx.config.token}")
    private String token;

    //校验token
    @Override
    public boolean checkSign(String signature, String timestamp, String nonce) {
        return SignatureUtil.check(token, signature, timestamp, nonce);
    }

}