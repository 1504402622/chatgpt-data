package cn.glfs.chatgpt.data.trigger.http;


import lombok.extern.slf4j.Slf4j;
import com.wechat.pay.java.core.notification.NotificationParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")//设置可跨域
@RequestMapping("/api/${app.config.api-version}/sale/")
public class SaleController {


    @Autowired(required = false)//若没有该bean不会报错
    private NotificationParser notificationParser;


}
