package cn.glfs.chatgpt.data.infrastructure.repository;

import cn.glfs.chatgpt.data.domain.order.model.aggregates.CreateOrderAggregate;
import cn.glfs.chatgpt.data.domain.order.model.entity.*;
import cn.glfs.chatgpt.data.domain.order.model.valobj.OrderStatusVO;
import cn.glfs.chatgpt.data.domain.order.model.valobj.PayStatusVO;
import cn.glfs.chatgpt.data.domain.order.repository.IOrderRepository;
import cn.glfs.chatgpt.data.infrastructure.dao.IOpenAIOrderDao;
import cn.glfs.chatgpt.data.infrastructure.dao.IOpenAIProductDao;
import cn.glfs.chatgpt.data.infrastructure.dao.IUserAccountDao;
import cn.glfs.chatgpt.data.infrastructure.po.OpenAIOrderPO;
import cn.glfs.chatgpt.data.infrastructure.po.OpenAIProductPO;
import cn.glfs.chatgpt.data.infrastructure.po.UserAccountPO;
import cn.glfs.chatgpt.data.types.enums.OpenAIProductEnableModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单仓储服务
 */
@Slf4j
@Repository
public class OrderRepository implements IOrderRepository {
    @Resource
    private IOpenAIOrderDao openAIOrderDao;
    @Resource
    private IOpenAIProductDao openAIProductDao;
    @Resource
    private IUserAccountDao userAccountDao;


    /**
     * 输入：购物车实体
     * 根据购物车信息（含用户id,商品id）查找用户订单信息
     * 返回：订单实体
     * @param shopCartEntity
     * @return
     */
    @Override
    public UnpaidOrderEntity queryUnpaidOrder(ShopCartEntity shopCartEntity) {
        OpenAIOrderPO openAIOrderPOReq = new OpenAIOrderPO();
        openAIOrderPOReq.setOpenid(shopCartEntity.getOpenid());
        openAIOrderPOReq.setProductId(shopCartEntity.getProductId());
        //据用户id，商品id和订单状态为0-创建完成查找订单信息
        OpenAIOrderPO openAIOrderPORes = openAIOrderDao.queryUnpaidOrder(openAIOrderPOReq);
        if(null==openAIOrderPORes) return null;
        return UnpaidOrderEntity.builder()
                .openid(shopCartEntity.getOpenid())
                .orderId(openAIOrderPORes.getOrderId())
                .productName(openAIOrderPORes.getProductName())
                .totalAmount(openAIOrderPORes.getTotalAmount())
                .payUrl(openAIOrderPORes.getPayUrl())
                .payStatus(PayStatusVO.get(openAIOrderPORes.getPayStatus()))
                .build();
    }

    /**
     * 输入：商品id
     * 根据商品id查找商品所有信息
     * 返回:商品实体
     * @param productId
     * @return
     */
    @Override
    public ProductEntity queryProduct(Integer productId) {
        //根据商品id查找商品所有信息
        OpenAIProductPO openAIProductPO = openAIProductDao.queryProductByProductId(productId);
        ProductEntity productEntity = new ProductEntity();
        productEntity.setProductId(openAIProductPO.getProductId());
        productEntity.setProductName(openAIProductPO.getProductName());
        productEntity.setProductDesc(openAIProductPO.getProductDesc());
        productEntity.setQuota(openAIProductPO.getQuota());
        productEntity.setProductModelTypes(openAIProductPO.getProductModelTypes());
        productEntity.setPrice(openAIProductPO.getPrice());
        productEntity.setEnable(OpenAIProductEnableModel.get(openAIProductPO.getIsEnabled()));
        return productEntity;
    }

    /**
     * 输入：聚合对象
     * 由下单聚合对象,创建一条订单数据
     * @param aggregate
     */
    @Override
    public void saveOrder(CreateOrderAggregate aggregate) {
        String openid = aggregate.getOpenid();
        ProductEntity product = aggregate.getProduct();
        OrderEntity order = aggregate.getOrder();
        OpenAIOrderPO openAIOrderPO = new OpenAIOrderPO();
        openAIOrderPO.setOpenid(openid);
        openAIOrderPO.setProductId(product.getProductId());
        openAIOrderPO.setProductName(product.getProductName());
        openAIOrderPO.setProductModelTypes(product.getProductModelTypes());
        openAIOrderPO.setProductQuota(product.getQuota());
        openAIOrderPO.setOrderId(order.getOrderId());
        openAIOrderPO.setOrderTime(order.getOrderTime());
        openAIOrderPO.setOrderStatus(order.getOrderStatus().getCode());
        openAIOrderPO.setTotalAmount(order.getTotalAmount());
        openAIOrderPO.setPayType(order.getPayTypeVO().getCode());
        openAIOrderPO.setPayStatus(PayStatusVO.WAIT.getCode());

        // 插入一条订单数据
        openAIOrderDao.insert(openAIOrderPO);
    }

    /**
     * 输入：用户实体
     * 由（用户id和订单id,修改订单地址,支付状态,时间
     * @param payOrderEntity
     */
    @Override
    public void updateOrderPayInfo(PayOrderEntity payOrderEntity) {
        OpenAIOrderPO openAIOrderPO = new OpenAIOrderPO();
        openAIOrderPO.setOpenid(payOrderEntity.getOpenid());
        openAIOrderPO.setOrderId(payOrderEntity.getOrderId());
        openAIOrderPO.setPayUrl(payOrderEntity.getPayUrl());
        openAIOrderPO.setPayStatus(payOrderEntity.getPayStatus().getCode());
        //根据用户id和订单id,修改订单地址,支付状态,时间
        openAIOrderDao.updateOrderPayInfo(openAIOrderPO);
    }


    /**
     * 输入：订单id，订单交易编号,总支付金额,支付时间
     * 当此用户没有支付时间时修改订单状态,支付完成,更新时间,交易订单号
     * 返回：修改成功返回true
     * @param orderId
     * @param transactionId
     * @param totalAmount
     * @param payTime
     * @return
     */
    @Override
    public boolean changeOrderPaySuccess(String orderId, String transactionId, BigDecimal totalAmount, Date payTime) {
        OpenAIOrderPO openAIOrderPO = new OpenAIOrderPO();
        openAIOrderPO.setOrderId(orderId);
        openAIOrderPO.setPayAmount(totalAmount);
        openAIOrderPO.setPayTime(payTime);
        openAIOrderPO.setTransactionId(transactionId);
        //由id和没有支付时间时,修改订单状态为1（等待发货）,支付状态1（支付完成）,和传参修改赋值支付金额,赋值支付时间,赋值交易单号,修改时间
        int count = openAIOrderDao.changeOrderPaySuccess(openAIOrderPO);
        return count == 1;
    }


    /**
     * 输入：订单id
     * 根据订单id,查找订单全部信息并注入到聚合对象
     * 输出：聚合对象
     * @param orderId
     * @return
     */
    @Override
    public CreateOrderAggregate queryOrder(String orderId) {
        //根据订单id,查找订单全部信息
        OpenAIOrderPO openAIOrderPO = openAIOrderDao.queryOrder(orderId);

        ProductEntity product = new ProductEntity();
        product.setProductId(openAIOrderPO.getProductId());
        product.setProductName(openAIOrderPO.getProductName());

        OrderEntity order = new OrderEntity();
        order.setOrderId(openAIOrderPO.getOrderId());
        order.setOrderTime(openAIOrderPO.getOrderTime());
        order.setOrderStatus(OrderStatusVO.get(openAIOrderPO.getOrderStatus()));
        order.setTotalAmount(openAIOrderPO.getTotalAmount());

        CreateOrderAggregate createOrderAggregate = new CreateOrderAggregate();
        createOrderAggregate.setOpenid(openAIOrderPO.getOpenid());
        createOrderAggregate.setOrder(order);
        createOrderAggregate.setProduct(product);

        return null;
    }

    /**
     * 输入：订单id
     * 设置订单发货完成,并修改用户信息（根据用户id更新剩余额度和总额度和更新时间）
     * @param orderId
     * @Transactional: 这是一个注解，用于声明被注解的方法将被包裹在一个事务中进行执行。
     * rollbackFor = Exception.class: 这个参数指定了哪些异常会触发事务回滚。在这里，Exception.class表示所有Exception及其子类的异常都会触发事务回滚。
     * timeout = 350: 这个参数指定了事务的超时时间，单位为秒。在这里，事务的超时时间被设置为350秒，即事务执行时间超过这个时间将被强制回滚。
     * propagation = Propagation.REQUIRED: 这个参数指定了事务的传播行为。Propagation.REQUIRED表示如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务。
     * isolation = Isolation.DEFAULT: 这个参数指定了事务的隔离级别。Isolation.DEFAULT表示使用默认的数据库隔离级别，通常是数据库的默认隔离级别。
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 350, propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public void deliverGoods(String orderId) {
        //根据订单,查找订单全部信息
        OpenAIOrderPO openAIOrderPO = openAIOrderDao.queryOrder(orderId);

        //1.变更发货状态
        //根据订单id和订单状态为1（等待发货）将订单状态修改为2发货完成
        int updateOrderStatusDeliverGoodsCount = openAIOrderDao.updateOrderStatusDeliverGoods(orderId);
        //若修改失败报运行时异常
        if(1 != updateOrderStatusDeliverGoodsCount) throw new RuntimeException("updateOrderStatusDeliverGoodsCount update count is not equal 1");

        //2.账户额度变更
        //根据用户id查询用户信息
        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(openAIOrderPO.getOpenid());
        log.info("发货前用户信息为：{}", userAccountPO);
        UserAccountPO userAccountPOReq = new UserAccountPO();
        userAccountPOReq.setOpenid(openAIOrderPO.getOpenid());
        userAccountPOReq.setTotalQuota(openAIOrderPO.getProductQuota());
        userAccountPOReq.setSurplusQuota(openAIOrderPO.getProductQuota());
        userAccountPOReq.setModelTypes(openAIOrderPO.getProductModelTypes());
        if (null != userAccountPO){
            // todo 待补全：用户使用模型权限递增
            //根据用户id更新剩余额度和总额度和更新时间
            int addAccountQuotaCount = userAccountDao.addAccountQuota(userAccountPOReq);
            if (1 != addAccountQuotaCount) throw new RuntimeException("addAccountQuotaCount update count is not equal 1");
        } else {
            log.info("不存在用户{}，发货时创建用户信息{}", openAIOrderPO.getOpenid(), userAccountPOReq);
            userAccountDao.insert(userAccountPOReq);
        }

    }

    /**
     * 根据订单状态1（等待发货），支付状态为1（支付完成）按id增序输出订单id，时间大于等于订单时间加上3分钟的时间间隔,最多输出10个，查找订单id
     * 输出：输出最多10条用户id
     * @return
     */
    @Override
    public List<String> queryReplenishmentOrder() {
        return openAIOrderDao.queryReplenishmentOrder();
    }

    /**
     * 根据订单状态0（创建完成），支付状态为0（等待支付）时间大于等于订单时间加上1分钟的时间间隔,最多输出10条，查找订单id
     * 输出：输出至多10条用户id
     * @return
     */
    @Override
    public List<String> queryNoPayNotifyOrder() {
        return openAIOrderDao.queryNoPayNotifyOrder();
    }

    /**
     * 根据订单状态0（创建完成），支付状态为0（等待支付）时间大于等于订单时间加上30分钟的时间间隔,最多输出50条
     * 输出：输出最多50条用户id
     * @return
     */
    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return openAIOrderDao.queryTimeoutCloseOrderList();
    }

    /**
     * 输入：订单id
     * 根据订单id和支付状态为0（等待支付）去修改订单状态为3（系统关单），支付状态为3（放弃支付），修改时间设为现在
     * 输出：修改成功返回true
     * @param orderId
     * @return
     */
    @Override
    public boolean changeOrderClose(String orderId) {
        return openAIOrderDao.changeOrderClose(orderId);
    }


    /**
     * 将所有可用的商品装到list中
     * 输出：返回可用商品集合
     * @return
     */
    @Override
    public List<ProductEntity> queryProductList() {
        //查找可用商品
        List<OpenAIProductPO> openAIProductPOList =  openAIProductDao.queryProductList();
        List<ProductEntity> productEntityList = new ArrayList<>(openAIProductPOList.size());
        for (OpenAIProductPO openAIProductPO : openAIProductPOList) {
            ProductEntity productEntity = new ProductEntity();
            productEntity.setProductId(openAIProductPO.getProductId());
            productEntity.setProductName(openAIProductPO.getProductName());
            productEntity.setProductDesc(openAIProductPO.getProductDesc());
            productEntity.setQuota(openAIProductPO.getQuota());
            productEntity.setPrice(openAIProductPO.getPrice());
            productEntityList.add(productEntity);
        }
        return productEntityList;
    }
}
