package cn.glfs.chatgpt.data.trigger.job;

import cn.glfs.chatgpt.data.domain.order.service.IOrderService;
//import cn.glfs.ltzf.payments.nativepay.NativePayService;
//import cn.glfs.ltzf.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
//import cn.glfs.ltzf.payments.nativepay.model.QueryOrderByOutTradeNoResponse;
import cn.hutool.core.collection.CollectionUtil;
import com.google.common.eventbus.EventBus;
//import com.wechat.pay.java.service.payments.model.Transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

/**
 * 订单状态修改失败导致无法发货, 订单补偿
 */
@Slf4j
@Component()
public class NoPayNotifyOrderJob {

    @Resource
    private IOrderService orderService;
//    @Autowired(required = false)
//    private NativePayService payService;

    @Resource
    private EventBus eventBus;

    @Value("${ltzf.sdk.config.merchant_id}")
    private String mchid;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");


    //任务每隔一分钟执行一次。
    @Scheduled(cron = "0 0/1 * * * ?")
    public void exec() {
//        try {
//            if (Objects.isNull(payService)) {
//                log.info("定时任务，订单支付状态更新。应用未配置支付渠道，任务不执行。");
//                return;
//            }
//            List<String> orderIds = orderService.queryNoPayNotifyOrder();
//            if (CollectionUtil.isEmpty(orderIds)) {
//                log.info("定时任务，订单支付状态更新，暂无未更新订单 orderId is null");
//                return;
//            }
//            for (String orderId : orderIds) {
//                // 查询结果
//                QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
//                request.setMchId(mchid);
//                request.setOutTradeNo(orderId);
//                QueryOrderByOutTradeNoResponse queryOrderByOutTradeNoResponse = payService.queryOrderByOutTradeNo(request);
//                if (queryOrderByOutTradeNoResponse.getCode() != 0) {
//                    log.info("定时任务，订单支付状态更新，当前订单未支付 orderId is {}", orderId);
//                    continue;
//                }
//                // 支付单号
//                QueryOrderByOutTradeNoResponse.Data data = queryOrderByOutTradeNoResponse.getData();
//                String payNo = data.getPayNo();
//                String totalFee = data.getTotalFee();
//                BigDecimal totalAmount = new BigDecimal(totalFee).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
//                String successTime = data.getSuccessTime();
//                // 更新订单
//                boolean isSuccess = orderService.changeOrderPaySuccess(orderId, payNo, totalAmount, dateFormat.parse(successTime));
//                if (isSuccess) {
//                    // 发布消息
//                    eventBus.post(orderId);
//                }
//            }
//        } catch (Exception e) {
//            log.error("定时任务，订单支付状态更新失败", e);
//        }
    }
}
