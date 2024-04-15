package cn.glfs.chatgpt.data.config;


import com.glfs.chatgpt.session.OpenAiSession;
import com.glfs.chatgpt.session.OpenAiSessionFactory;
import com.glfs.chatgpt.session.defaults.DefaultOpenAiSessionFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 使用会话工厂创建会话
 */
@Configuration
@EnableConfigurationProperties(ChatGPTSDKConfigProperties.class)//指定的配置类实例化为 Bean，并提供属性注入
public class ChatGPTSDKConfig {
    @Bean
    public OpenAiSession openAiSession(ChatGPTSDKConfigProperties properties){

        //1.配置文件
        com.glfs.chatgpt.session.Configuration configuration = new com.glfs.chatgpt.session.Configuration();
        configuration.setApiHost(properties.getApiHost());
        configuration.setApiKey(properties.getApiKey());
        configuration.setAuthToken(properties.getAuthToken());
        //2.会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        //3.创建会话
        return factory.openSession();
    }
}
