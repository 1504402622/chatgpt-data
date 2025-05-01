package cn.glfs.chatgpt.data.domain.order.model.entity;


import cn.glfs.chatgpt.data.types.enums.OpenAIProductEnableModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 商品实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductEntity {

    /**
     * 商品id
     */
    private Integer productId;
    /**
     * 商品名称
     */
    private String productName;
    /**
     * 商品描述
     */
    private String productDesc;
    /**
     * 额度次数
     */
    private Integer quota;
    /**
     * 商品类型
     */
    private String productModelTypes;
    /**
     * 商品价格
     */
    private BigDecimal price;
    /**
     * 商品状态：0-无效，1-有效
     */
    private OpenAIProductEnableModel enable;

    /**
     * 是否有效 true = 有效，false = 无效
     */
    public boolean isAvailable(){
        return OpenAIProductEnableModel.OPEN.equals(enable);
    }
}
