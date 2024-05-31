package cn.glfs.chatgpt.data.infrastructure.dao;

import cn.glfs.chatgpt.data.infrastructure.po.OpenAIOrderPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 订单dao
 */
@Mapper
public interface IOpenAIOrderDao {

    /**
     * 根据用户id，商品id和订单状态为0-创建完成
     * 查找order_id, product_name, pay_url, pay_status, total_amount指定了查询结果按照id列进行降序排序。只返回满足上述条件的一行记录。
     * @param openAIOrderPOReq
     * @return
     */
    OpenAIOrderPO queryUnpaidOrder(OpenAIOrderPO openAIOrderPOReq);

    /**
     * 插入一条订单数据
     * @param order
     */
    void insert(OpenAIOrderPO order);

    /**
     * 根据用户id和订单id修改
     * 订单地址,支付状态,修改时间
     * @param openAIOrderPO
     */
    void updateOrderPayInfo(OpenAIOrderPO openAIOrderPO);

    /**
     * 根据id和没有支付时间时
     * 修改订单状态为1（等待发货）,支付状态1（支付完成）,和传参修改赋值支付金额,赋值支付时间,赋值交易单号,修改时间
     * @param openAIOrderPO
     * @return
     */
    int changeOrderPaySuccess(OpenAIOrderPO openAIOrderPO);

    /**
     * 根据订单id
     * 查找订单全部信息
     * @param orderId
     * @return
     */
    OpenAIOrderPO queryOrder(String orderId);

    /**
     * 根据订单id和订单状态为1（等待发货）
     * 将订单状态修改为2发货完成
     * @param orderId
     * @return
     */
    int updateOrderStatusDeliverGoods(String orderId);

    /**
     * 根据订单状态1（等待发货），支付状态为1（支付完成）按id增序输出订单id，时间大于等于订单时间加上3分钟的时间间隔,最多输出10个
     * 查找订单id
     * @return
     */
    List<String> queryReplenishmentOrder();

    /**
     * 根据订单状态0（创建完成），支付状态为0（等待支付）时间大于等于订单时间加上1分钟的时间间隔,最多输出10条
     * 查找订单id
     * @return
     */
    List<String> queryNoPayNotifyOrder();

    /**
     * 根据订单状态0（创建完成），支付状态为0（等待支付）时间大于等于订单时间加上30分钟的时间间隔,最多输出50条
     * 查找订单id
     * @return
     */
    List<String> queryTimeoutCloseOrderList();

    /**
     * 根据订单id和支付状态为0（等待支付）
     * 修改订单状态为3（系统关单），支付状态为3（放弃支付），修改时间设为现在
     * @param orderId
     * @return
     */
    boolean changeOrderClose(String orderId);
}

