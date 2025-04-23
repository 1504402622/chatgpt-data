package cn.glfs.chatgpt.data.config;


import cn.glfs.chatglm.session.Configuration;
import cn.glfs.chatglm.session.OpenAiSession;
import cn.glfs.chatglm.session.OpenAiSessionFactory;
import cn.glfs.chatglm.session.defaults.DefaultOpenAiSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;



@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(ChatGLMSDKConfigProperties.class)
public class ChatGLMSDKConfig {

    @Bean(name = "chatGlMOpenAiSession")
    @ConditionalOnProperty(value = "chatglm.sdk.config.enabled", havingValue = "true", matchIfMissing = false)
    public OpenAiSession openAiSession(ChatGLMSDKConfigProperties properties) {
        // 1. 配置文件
        Configuration configuration = new Configuration();
        configuration.setApiHost(properties.getApiHost());
        configuration.setApiSecretKey(properties.getApiSecretKey());

        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);

        // 3. 开启会话
        return factory.openSession();
    }

}
