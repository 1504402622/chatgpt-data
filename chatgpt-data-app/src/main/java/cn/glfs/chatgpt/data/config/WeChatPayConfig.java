package cn.glfs.chatgpt.data.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(WeChatPayConfigProperties.class)
public class WeChatPayConfig {
}
