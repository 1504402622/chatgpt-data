package cn.glfs.chatgpt.data;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 */

@SpringBootApplication
@Configurable
@EnableScheduling//支持schedule支持@Scheduled 注解用于标记一个方法，以指示 Spring 在特定的时间间隔或固定的时间点执行该方法。
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
