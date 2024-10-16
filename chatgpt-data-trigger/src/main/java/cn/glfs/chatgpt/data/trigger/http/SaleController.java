package cn.glfs.chatgpt.data.trigger.http;


import cn.glfs.chatgpt.data.domain.auth.service.IAuthService;
import cn.glfs.chatgpt.data.domain.order.model.entity.PayOrderEntity;
import cn.glfs.chatgpt.data.domain.order.model.entity.ProductEntity;
import cn.glfs.chatgpt.data.domain.order.model.entity.ShopCartEntity;
import cn.glfs.chatgpt.data.domain.order.service.IOrderService;
import cn.glfs.chatgpt.data.trigger.http.dto.SaleProductDTO;
import cn.glfs.chatgpt.data.types.common.Constants;
import cn.glfs.chatgpt.data.types.model.Response;
import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.EventBus;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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

    // 商品列表查询
    @RequestMapping(value = "query_product_list", method = RequestMethod.GET)
    public Response<List<SaleProductDTO>> queryProductList(@RequestHeader("Authorization") String token) {
        try {
            // 1.token校验
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<List<SaleProductDTO>>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            // 2.查询商品
            List<ProductEntity> productEntities = orderService.queryProductList();
            log.info("商品查询 {}", JSON.toJSONString(productEntities));

            List<SaleProductDTO> mallProductDTOS = new ArrayList<>();
            for (ProductEntity productEntity : productEntities) {
                SaleProductDTO mallProductDTO = SaleProductDTO.builder()
                        .productId(productEntity.getProductId())
                        .productName(productEntity.getProductName())
                        .productDesc(productEntity.getProductDesc())
                        .price(productEntity.getPrice())
                        .quota(productEntity.getQuota())
                        .build();
                mallProductDTOS.add(mallProductDTO);
            }

            // 3.返回列表
            return Response.<List<SaleProductDTO>>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(mallProductDTOS)
                    .build();
        } catch (Exception e) {
            log.error("商品查询失败", e);
            return Response.<List<SaleProductDTO>>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    // 用户商品下单
    @RequestMapping(value = "create_pay_order", method = RequestMethod.POST)
    public Response<String> createParOrder(@RequestHeader("Authorization") String token, @RequestParam Integer productId) {
        try {
            // 1. Token 校验
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<String>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            // 2. Token 解析
            String openid = authService.openid(token);
            assert null != openid;
            log.info("用户商品下单，根据商品ID创建支付单开始 openid:{} productId:{}", openid, productId);

            ShopCartEntity shopCartEntity = ShopCartEntity.builder()
                    .openid(openid)
                    .productId(productId)
                    .build();

            PayOrderEntity payOrder = orderService.createOrder(shopCartEntity);
            log.info("用户商品下单，根据商品ID创建支付单完成 openid: {} productId: {} orderPay: {}", openid, productId, payOrder.toString());

            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("用户商品下单, 根据商品ID创建支付失败", e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }


//    // 支付回调
//    @PostMapping("pay_notify")
//    public void payNotify(@RequestBody NotifyRequest notify, HttpServletRequest request, HttpServletResponse response) throws IOException {
//        try {
//            if (StringUtils.equals(notify.getCode(), "0")) {
//                // 支付单号
//                String outTradeNo = notify.getOutTradeNo();
//                String payNo = notify.getPayNo();
//                String totalFee = notify.getTotalFee();
//                String successTime = notify.getSuccessTime();
//                log.info("支付成功 orderId:{} total:{} successTime: {}", outTradeNo, totalFee, successTime);
//                // 更新订单
//               //  boolean isSuccess = orderService.changeOrderPaySuccess(outTradeNo, payNo, new BigDecimal(total).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP), dateFormat.parse(successTime));
////                if (isSuccess) {
////                    // 发布消息
////                    // eventBus.post(orderId);
////                }
//                response.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>");
//            } else {
//                response.getWriter().write("<xml><return_code><![CDATA[FAIL]]></return_code></xml>");
//            }
//        } catch (Exception e) {
//            log.error("支付失败", e);
//            response.getWriter().write("<xml><return_code><![CDATA[FAIL]]></return_code></xml>");
//        }
//    }
}
