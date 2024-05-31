package cn.glfs.chatgpt.data.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微信支付配置信息
 */
@Data
@ConfigurationProperties(prefix = "wxpay.config", ignoreInvalidFields = true)//以 "wxpay.config" 开头的属性都会被绑定到被注解的类的属性上,并忽略无效字符
public class WeChatPayConfigProperties {

    /** 状态：open = 开启,close 关闭 **/
    private boolean enable;
    /** 申请支主体的appid **/
    private String appid;
    /** 商户号 **/
    private String mchid;
    /** 回调地址 **/
    private String notifyUrl;
    /** 商户API私钥路径 **/
    private String privateKeyPath;
    /** 商户证书序列号：openssl x509 -in apiclient_cert.pem -noout -serial **/
    private String merchantSerialNumber;
    /** 商户APIV3密钥 **/
    private String apiV3Key;
}
