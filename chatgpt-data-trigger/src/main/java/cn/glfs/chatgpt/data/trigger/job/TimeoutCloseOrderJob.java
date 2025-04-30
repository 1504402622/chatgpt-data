package cn.glfs.chatgpt.data.trigger.job;


import cn.glfs.chatgpt.data.domain.order.service.IOrderService;
//import cn.glfs.ltzf.payments.nativepay.NativePayService;
import cn.glfs.ltzf.payments.nativepay.NativePayService;
import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 超30分钟未支付，超时关单
 */
@Slf4j
@Component
public class TimeoutCloseOrderJob {

    @Resource
    private IOrderService orderService;

    @Autowired(required = false)
    private NativePayService payService;

    @Value("${ltzf.sdk.config.merchant_id}")
    private String mchid;

    @Scheduled(cron = "0 0/10 * * * ?")//注解来设置任务每隔一分钟执行一次。
    public void exec() {
        try {
            if (Objects.isNull(payService)) {
                log.info("定时任务，订单支付状态更新。应用未配置支付渠道，任务不执行");
                return;
            }
            List<String> orderIds = orderService.queryTimeoutCloseOrderList();
            if (CollectionUtil.isEmpty(orderIds)) {
                log.info("定时任务, 超时30分钟订单关闭, 暂无超时未支付订单, orderIds is null");
                return;
            }
            for (String orderId : orderIds) {
                boolean status = orderService.changeOrderClose(orderId);
                // 微信关单(假装关单，呜呜呜蓝兔支付没有关单api，可恶)
                log.info("定时任务，超时30分钟订单关闭 orderId: {} status：{}", orderId, status);
            }
        } catch (Exception e) {
            log.error("定时任务，超时30分钟订单关闭失败", e);
        }
    }

}
