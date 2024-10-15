package cn.glfs.chatgpt.data.trigger.job;

import cn.glfs.chatgpt.data.domain.order.service.IOrderService;
import cn.hutool.core.collection.CollectionUtil;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * mq失败：发货补偿
 */
@Slf4j
@Component()
public class OrderReplenishmentJob {
    @Resource
    private IOrderService orderService;
    @Resource
    private EventBus eventBus;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void exec() {
        try {
            List<String> orderIds = orderService.queryReplenishmentOrder();
            if (CollectionUtil.isEmpty(orderIds)) {
                log.info("定时任务, 订单补货不存在，查询 orderIds is null");
                return;
            }
            for (String orderId : orderIds) {
                log.info("定时任务，订单补货开始。orderId: {}", orderId);
                eventBus.post(orderId);
            }
        } catch (Exception e) {
            log.error("定时任务，订单补货失败。", e);
        }
    }


}
