package cn.glfs.chatgpt.data.config;



import com.glfs.deepseek.session.Configuration;
import com.glfs.deepseek.session.OpenAiSession;
import com.glfs.deepseek.session.OpenAiSessionFactory;
import com.glfs.deepseek.session.defaults.DefaultOpenAiSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(DeepSeekSDKConfigProperties.class)
public class DeepSeekSDKConfig {

    @Bean(name = "deepSeekOpenAiSession")
    @ConditionalOnProperty(value = "deepseek.sdk.config.enabled", havingValue = "true", matchIfMissing = false)
    public OpenAiSession openAiSession(DeepSeekSDKConfigProperties properties) {
        // 1. 配置文件
        Configuration configuration = new Configuration();
        configuration.setApiHost(properties.getApiHost());
        configuration.setApiKey(properties.getApiKey());

        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);

        // 3. 开启会话
        return factory.openSession();
    }

}
