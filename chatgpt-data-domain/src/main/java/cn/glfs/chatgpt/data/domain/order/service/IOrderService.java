package cn.glfs.chatgpt.data.domain.order.service;



import cn.glfs.chatgpt.data.domain.order.model.aggregates.CreateOrderAggregate;
import cn.glfs.chatgpt.data.domain.order.model.entity.PayOrderEntity;
import cn.glfs.chatgpt.data.domain.order.model.entity.ProductEntity;
import cn.glfs.chatgpt.data.domain.order.model.entity.ShopCartEntity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @description 订单服务
 * 1. 用户下单 createOrder
 * @create 2023-10-05 10:49
 */
public interface IOrderService {

    /**
     * 用户下单，通过购物车信息，返回下单后的支付单
     *
     * @param shopCartEntity 简单购物车
     * @return 支付单实体对象
     */
    PayOrderEntity createOrder(ShopCartEntity shopCartEntity);

    /**
     * 变更；订单支付成功
     */
    boolean changeOrderPaySuccess(String orderId, String transactionId, BigDecimal totalAmount, Date payTime);

    /**
     * 查询订单信息
     *
     * @param orderId 订单ID
     * @return 查询结果
     */
    CreateOrderAggregate queryOrder(String orderId);

    /**
     * 订单商品发货
     *
     * @param orderId 订单ID
     */
    void deliverGoods(String orderId);

    /**
     * 查询待补货订单
     */
    List<String> queryReplenishmentOrder();

    /**
     * 查询有效期内，未接收到支付回调的订单
     */
    List<String> queryNoPayNotifyOrder();

    /**
     * 查询超时15分钟，未支付订单
     */
    List<String> queryTimeoutCloseOrderList();

    /**
     * 变更；订单支付关闭
     */
    boolean changeOrderClose(String orderId);

    /**
     * 查询商品列表
     */
    List<ProductEntity> queryProductList();

    /**
     * 判断订单是否已关单
     */
    boolean isOrderClosed(String orderId);

    /**
     * 订单退款
     */
    void refundOrder(String orderId);
}