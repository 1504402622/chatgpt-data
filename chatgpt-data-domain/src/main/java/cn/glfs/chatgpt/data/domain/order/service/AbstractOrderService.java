package cn.glfs.chatgpt.data.domain.order.service;

import cn.glfs.chatgpt.data.domain.order.model.aggregates.CreateOrderAggregate;
import cn.glfs.chatgpt.data.domain.order.model.entity.*;
import cn.glfs.chatgpt.data.domain.order.model.valobj.PayStatusVO;
import cn.glfs.chatgpt.data.domain.order.repository.IOrderRepository;
import cn.glfs.chatgpt.data.types.common.Constants;
import cn.glfs.chatgpt.data.types.exception.ChatGPTException;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class AbstractOrderService implements IOrderService {
    @Resource
    protected IOrderRepository orderRepository;

    @Override
    public PayOrderEntity createOrder(ShopCartEntity shopCartEntity) {
        return null;
    }

    protected abstract OrderEntity doSaveOrder(String openid, ProductEntity productEntity);

    protected abstract PayOrderEntity doPrepayOrder(String openid, String orderId, String productName, BigDecimal amountTotal);
}
