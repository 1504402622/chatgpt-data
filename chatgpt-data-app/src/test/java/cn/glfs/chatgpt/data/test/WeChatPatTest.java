package cn.glfs.chatgpt.data.test;

import cn.glfs.ltzf.payments.nativepay.NativePayService;
import cn.glfs.ltzf.payments.nativepay.model.PrepayRequest;
import cn.glfs.ltzf.payments.nativepay.model.PrepayResponse;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class WeChatPatTest {

    @Autowired
    private NativePayService nativePayService;

    @Test
    public void testNativePayService() throws Exception {
        // 1. 请求参数
        PrepayRequest request = new PrepayRequest();
        request.setMchId("1676472264");
        request.setOutTradeNo(RandomStringUtils.randomNumeric(8));//RandomStringUtils.randomNumeric(8)
        request.setTotalFee("0.01");
        request.setBody("QQ公仔");
        request.setNotifyUrl("https://api.glfskk.top/api/v1/sale/pay_notify");

        // 2.发起支付请求并返回
        PrepayResponse response = nativePayService.prepay(request);

        log.info("请求参数:{}", JSON.toJSONString(request));
        log.info("应答结果:{}", JSON.toJSONString(response));
    }
}
