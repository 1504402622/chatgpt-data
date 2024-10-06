package cn.glfs.chatgpt.data.config;


import cn.glfs.ltzf.factory.Configuration;
import cn.glfs.ltzf.factory.PayFactory;
import cn.glfs.ltzf.factory.defaults.DefaultPayFactory;
import cn.glfs.ltzf.payments.app.AppPayService;
import cn.glfs.ltzf.payments.h5.H5PayService;
import cn.glfs.ltzf.payments.jsapi.JSPayService;
import cn.glfs.ltzf.payments.jump_h5.JumpH5PayService;
import cn.glfs.ltzf.payments.nativepay.NativePayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(LtzfSDKConfigProperties.class)
public class LtzfSDKConfig {

    @Bean(name = "payFactory")
    @ConditionalOnProperty(value = "ltzf.sdk.config.enabled", havingValue = "true", matchIfMissing = false)
    public PayFactory payFactory(LtzfSDKConfigProperties properties) {
        Configuration configuration = new Configuration(
                properties.getAppId(),
                properties.getMerchantId(),
                properties.getPartnerKey()
        );

        return new DefaultPayFactory(configuration);
    }

    // 以下bean均可使用方法，支付请求prepay，订单退款refundOrder，查询订单getPayOrder，查询退款结果getRefundOrder
    // 扫码支付
    @Bean(name = "nativePayService")
    @ConditionalOnProperty(value = "ltzf.sdk.config.enabled", havingValue = "true", matchIfMissing = false)
    public NativePayService nativePayService(PayFactory payFactory) {
        log.info("蓝兔支付 SDK 启动成功，扫码支付服务已装配");
        return payFactory.nativePayService();
    }

    // H5支付
    @Bean(name = "h5PayService")
    @ConditionalOnProperty(value = "ltzf.sdk.config.enabled", havingValue = "true", matchIfMissing = false)
    public H5PayService h5PayService(PayFactory payFactory) {
        log.info("蓝兔支付 SDK 启动成功，H5支付服务已装配");
        return payFactory.h5PayService();
    }

    // app支付
    @Bean(name = "appPayService")
    @ConditionalOnProperty(value = "ltzf.sdk.config.enabled", havingValue = "true", matchIfMissing = false)
    public AppPayService appPayService(PayFactory payFactory) {
        log.info("蓝兔支付 SDK 启动成功，H5支付服务已装配");
        return payFactory.appPayService();
    }

    // 跳转支付支付
    @Bean(name = "jumpH5PayService")
    @ConditionalOnProperty(value = "ltzf.sdk.config.enabled", havingValue = "true", matchIfMissing = false)
    public JumpH5PayService jumpH5PayService(PayFactory payFactory) {
        log.info("蓝兔支付 SDK 启动成功，H5支付服务已装配");
        return payFactory.jumpH5PayService();
    }

    // 公众号支付
    @Bean(name = "jsPayService")
    @ConditionalOnProperty(value = "ltzf.sdk.config.enabled", havingValue = "true", matchIfMissing = false)
    public JSPayService jsPayService(PayFactory payFactory) {
        log.info("蓝兔支付 SDK 启动成功，H5支付服务已装配");
        return payFactory.jsPayService();
    }

}
