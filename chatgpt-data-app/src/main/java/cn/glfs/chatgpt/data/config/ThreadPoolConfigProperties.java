package cn.glfs.chatgpt.data.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 线程池配置，配置文件映射成配置类
 */
@Data
@ConfigurationProperties(prefix = "thread.pool.executor.config",ignoreInvalidFields = true)//在配置类中找到与下面变量同名的属性并赋值，不匹配的属性会被忽略
public class ThreadPoolConfigProperties {
    //核心线程数
    private Integer corePoolSize = 20;
    //最大线程数
    private Integer maxPoolSize = 200;
    //最大等待时间
    private Long keepAliveTime = 10L;
    //最大队列数
    private Integer blockQueueSize = 5000;
    /*
     * 拒绝策略
     * AbortPolicy：当线程池的任务队列已满且无法继续添加新任务时，会直接抛出RejectedExecutionException异常，表示拒绝该任务的提交
     * DiscardPolicy：当线程池的任务队列已满且无法继续添加新任务时，会直接丢弃该任务，不会抛出异常。
     * DiscardOldestPolicy：当线程池的任务队列已满且无法继续添加新任务时，会删除队列中最早进入队列的任务，然后尝试再次将新任务加入队列。
     * CallerRunsPolicy：当线程池的任务队列已满且无法继续添加新任务时，新提交的任务会由添加任务的线程（调用者）自己执行，从而避免任务丢失，但可能会影响调用者的性能。
     */
    private String policy = "AbortPolicy";
}
