package cn.glfs.chatgpt.data.domain.order.service;

import cn.glfs.chatgpt.data.domain.order.model.aggregates.CreateOrderAggregate;
import cn.glfs.chatgpt.data.domain.order.model.entity.OrderEntity;
import cn.glfs.chatgpt.data.domain.order.model.entity.PayOrderEntity;
import cn.glfs.chatgpt.data.domain.order.model.entity.ProductEntity;
import cn.glfs.chatgpt.data.domain.order.model.valobj.OrderStatusVO;
import cn.glfs.chatgpt.data.domain.order.model.valobj.PayStatusVO;
import cn.glfs.chatgpt.data.domain.order.model.valobj.PayTypeVO;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class OrderService extends AbstractOrderService{


    @Override
    protected OrderEntity doSaveOrder(String openid, ProductEntity productEntity) {
        return null;
    }

    /**
     *
     * @param openid
     * @param orderId
     * @param productName
     * @param amountTotal
     * @return
     */
    @Override
    protected PayOrderEntity doPrepayOrder(String openid, String orderId, String productName, BigDecimal amountTotal) {
        return null;
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
