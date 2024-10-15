package cn.glfs.chatgpt.data.trigger.http;


import cn.glfs.chatgpt.data.domain.auth.service.IAuthService;
import cn.glfs.chatgpt.data.domain.order.service.IOrderService;
import cn.glfs.ltzf.common.entity.NotifyRequest;
import com.google.common.eventbus.EventBus;
import com.wechat.pay.java.service.partnerpayments.nativepay.model.Transaction;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;

/**
 * 下单
 */
@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")//设置可跨域
@RequestMapping("/api/${app.config.api-version}/sale/")
public class SaleController {


    @Resource
    private IOrderService orderService;
    @Resource
    private IAuthService authService;
    @Resource
    private EventBus eventBus;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    // 支付回调
    @PostMapping("pay_notify")
    public void payNotify(@RequestBody NotifyRequest notify, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (StringUtils.equals(notify.getCode(), "0")) {
                // 支付单号
                String outTradeNo = notify.getOutTradeNo();
                String payNo = notify.getPayNo();
                String totalFee = notify.getTotalFee();
                String successTime = notify.getSuccessTime();
                log.info("支付成功 orderId:{} total:{} successTime: {}", outTradeNo, totalFee, successTime);
                // 更新订单
               //  boolean isSuccess = orderService.changeOrderPaySuccess(outTradeNo, payNo, new BigDecimal(total).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP), dateFormat.parse(successTime));
//                if (isSuccess) {
//                    // 发布消息
//                    // eventBus.post(orderId);
//                }
                response.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>");
            } else {
                response.getWriter().write("<xml><return_code><![CDATA[FAIL]]></return_code></xml>");
            }
        } catch (Exception e) {
            log.error("支付失败", e);
            response.getWriter().write("<xml><return_code><![CDATA[FAIL]]></return_code></xml>");
        }
    }
}
