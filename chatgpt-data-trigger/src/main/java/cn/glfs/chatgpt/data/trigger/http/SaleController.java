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
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
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



    @PostMapping("/pay_notify")
    public void payNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 从表单中获取参数
            String code = request.getParameter("code");
            String outTradeNo = request.getParameter("out_trade_no");
            String payNo = request.getParameter("pay_no");
            String totalFee = request.getParameter("total_fee");
            String successTime = request.getParameter("success_time");

            if ("0".equals(code)) {
                boolean isSuccess = orderService.changeOrderPaySuccess(outTradeNo, payNo, new BigDecimal(totalFee), dateFormat.parse(successTime));
                if (isSuccess) {
                    eventBus.post(outTradeNo);
                }
                // 返回响应
                PrintWriter writer = response.getWriter();
                writer.write("<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>");
            } else {
                PrintWriter writer = response.getWriter();
                writer.write("<xml><return_code><![CDATA[FAIL]]></return_code></xml>");
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 处理异常，可根据实际情况返回合适的错误响应
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
