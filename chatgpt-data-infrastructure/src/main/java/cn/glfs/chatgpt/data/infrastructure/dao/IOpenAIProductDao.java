package cn.glfs.chatgpt.data.infrastructure.dao;

import cn.glfs.chatgpt.data.infrastructure.po.OpenAIProductPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品dao
 */
@Mapper
public interface IOpenAIProductDao {
    /**
     * 根据商品id查找商品所有信息
     * @param productId
     * @return
     */
    OpenAIProductPO queryProductByProductId(Integer productId);

    /**
     * 查找可用商品
     * @return
     */
    List<OpenAIProductPO> queryProductList();
}
