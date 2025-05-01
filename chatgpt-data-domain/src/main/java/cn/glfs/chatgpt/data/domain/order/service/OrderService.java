package cn.glfs.chatgpt.data.domain.order.service;

import cn.glfs.chatgpt.data.domain.order.model.aggregates.CreateOrderAggregate;
import cn.glfs.chatgpt.data.domain.order.model.entity.OrderEntity;
import cn.glfs.chatgpt.data.domain.order.model.entity.PayOrderEntity;
import cn.glfs.chatgpt.data.domain.order.model.entity.ProductEntity;

import cn.glfs.chatgpt.data.domain.order.model.valobj.OrderStatusVO;
import cn.glfs.chatgpt.data.domain.order.model.valobj.PayStatusVO;
import cn.glfs.chatgpt.data.domain.order.model.valobj.PayTypeVO;
import cn.glfs.ltzf.payments.nativepay.NativePayService;
import cn.glfs.ltzf.payments.nativepay.model.PrepayRequest;
import cn.glfs.ltzf.payments.nativepay.model.PrepayResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class OrderService extends AbstractOrderService{

    @Value("${ltzf.sdk.config.app_id}")
    private String appid;
    @Value("${ltzf.sdk.config.merchant_id}")
    private String mchid;
    @Value("${ltzf.sdk.config.notify_url}")
    private String notifyUrl;
    @Autowired(required = false)
    private NativePayService payService;


    @Override
    protected OrderEntity doSaveOrder(String openid, ProductEntity productEntity) {
        OrderEntity orderEntity = new OrderEntity();
        // 数据库有幂等拦截，如果有重复的订单ID会报错主键冲突。如果是公司里一般会有专门的雪花算法UUID服务
        orderEntity.setOrderId(RandomStringUtils.randomNumeric(12));
        orderEntity.setOrderTime(new Date());
        orderEntity.setOrderStatus(OrderStatusVO.CREATE);
        orderEntity.setTotalAmount(productEntity.getPrice());
        orderEntity.setPayTypeVO(PayTypeVO.WEIXIN_NATIVE);
        orderEntity.setProductModelTypes(productEntity.getProductModelTypes());
        // 聚合信息
        CreateOrderAggregate aggregate = CreateOrderAggregate.builder()
                .openid(openid)
                .product(productEntity)
                .order(orderEntity)
                .build();
        // 保存订单；订单和支付，是2个操作。
        // 一个是数据库操作，一个是HTTP操作。所以不能一个事务处理，只能先保存订单再操作创建支付单，如果失败则需要任务补偿
        orderRepository.saveOrder(aggregate);
        return orderEntity;
    }


    @Override
    protected PayOrderEntity doPrepayOrder(String openid, String orderId, String productName, BigDecimal amountTotal) throws Exception {
        PrepayRequest request = new PrepayRequest();
        request.setMchId(mchid);
        request.setOutTradeNo(orderId);//RandomStringUtils.randomNumeric(8)
        request.setTotalFee(amountTotal.toString());
        request.setBody(productName);
        request.setNotifyUrl(notifyUrl);

        // 创建微信支付单，如果你有多种支付方式，则可以根据支付类型的策略模式进行创建支付单
        String codeUrl = "";
        if (Objects.nonNull(payService)) {
            PrepayResponse prepay = payService.prepay(request);
            log.info("请求支付响应为-prepay:{}", prepay);
            codeUrl = prepay.getData().getQrcodeUrl();
        } else {
            codeUrl = "因你未配置支付渠道，所以暂时不能生成有效的支付URL。请配置支付渠道后，在application-dev.yml中配置支付渠道信息";
        }


        PayOrderEntity payOrderEntity = PayOrderEntity.builder()
                .openid(openid)
                .orderId(orderId)
                .payUrl(codeUrl)
                .payStatus(PayStatusVO.WAIT)
                .build();

        // 更新订单支付信息
        orderRepository.updateOrderPayInfo(payOrderEntity);
        return payOrderEntity;
    }

    @Override
    public boolean changeOrderPaySuccess(String orderId, String transactionId, BigDecimal totalAmount, Date payTime) {
        return orderRepository.changeOrderPaySuccess(orderId, transactionId, totalAmount, payTime);
    }

    @Override
    public CreateOrderAggregate queryOrder(String orderId) {
        return orderRepository.queryOrder(orderId);
    }

    @Override
    public void deliverGoods(String orderId) {
        orderRepository.deliverGoods(orderId);
    }

    @Override
    public List<String> queryReplenishmentOrder() {
        return orderRepository.queryReplenishmentOrder();
    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
        return orderRepository.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return orderRepository.queryTimeoutCloseOrderList();
    }

    @Override
    public boolean changeOrderClose(String orderId) {
        return orderRepository.changeOrderClose(orderId);
    }

    @Override
    public List<ProductEntity> queryProductList() {
        return orderRepository.queryProductList();
    }
}
