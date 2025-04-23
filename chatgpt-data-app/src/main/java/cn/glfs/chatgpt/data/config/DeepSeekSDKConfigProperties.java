package cn.glfs.chatgpt.data.config;



import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ChatGPT配置文件映射成实体类
 */
@Data
@ConfigurationProperties(prefix = "deepseek.sdk.config", ignoreInvalidFields = true)//在配置类中找到与下面变量同名的属性并赋值，不匹配的属性会被忽略
public class DeepSeekSDKConfigProperties {

    /** 状态；open = 开启、close 关闭 */
    private boolean enable;
    /** 转发地址 */
    private String apiHost;
    private String apiKey;
}
