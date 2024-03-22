package cn.glfs.chatgpt.data.config;



import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ChatGPT配置文件映射成实体类
 */
@Data
@ConfigurationProperties(prefix = "chatgpt.sdk.config", ignoreInvalidFields = true)//在配置类中找到与下面变量同名的属性并赋值，不匹配的属性会被忽略
public class ChatGPTSDKConfigProperties {
    private String apiHost;
    private String apiKey;
    private String authToken;

}
