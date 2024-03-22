package cn.glfs.chatgpt.data.domain.weixin.service;

/**
 * 获取验签接口（有验签才算是返回成功，微信服务器才会发送给用户消息）
 */
public interface IWeiXinValidateService {

    boolean checkSign(String signature, String timestamp, String nonce);

}
