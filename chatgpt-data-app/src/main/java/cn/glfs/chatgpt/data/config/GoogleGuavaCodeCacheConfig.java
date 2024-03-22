package cn.glfs.chatgpt.data.config;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;


/**
 * 将验证码和用户id缓存存储到Guava中（比redis更加轻量级）
 */
@Configuration
public class GoogleGuavaCodeCacheConfig {

    //guava也是键值对存储的存储用户id和验证码
    @Bean(name = "codeCache")
    public Cache<String,String> codeCache(){
        return CacheBuilder.newBuilder()
                //设置了缓存项在写入一定时间后过期的时间为 3 分钟
                .expireAfterWrite(3, TimeUnit.MINUTES)
                .build();
    }

}
