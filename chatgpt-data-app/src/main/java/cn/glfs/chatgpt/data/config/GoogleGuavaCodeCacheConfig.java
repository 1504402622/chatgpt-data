package cn.glfs.chatgpt.data.config;


import cn.glfs.chatgpt.data.trigger.mq.OrderPaySuccessListener;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;


/**
 * 将验证码和用户id缓存存储到Guava中（比redis更加轻量级）
 */
@Configuration
public class GoogleGuavaCodeCacheConfig {

    //guava存储用户id和验证码缓存
    @Bean(name = "codeCache")
    public Cache<String,String> codeCache(){
        return CacheBuilder.newBuilder()
                //设置了缓存项在写入一定时间后过期的时间为 3 分钟
                .expireAfterWrite(3, TimeUnit.MINUTES)
                .build();
    }

    //guava对用户的免费次数已用次数缓存
    @Bean(name = "visitCache")
    public Cache<String, Integer> visitCache() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(12, TimeUnit.HOURS)
                .build();
    }

    //发货监听 OrderPaySuccessListener 配置
    @Bean
    public EventBus eventBusListener(OrderPaySuccessListener listener){
        EventBus eventBus=new EventBus();
        eventBus.register(listener);
        return eventBus;
    }
}
