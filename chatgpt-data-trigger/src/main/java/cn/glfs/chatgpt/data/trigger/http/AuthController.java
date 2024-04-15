package cn.glfs.chatgpt.data.trigger.http;


import cn.glfs.chatgpt.data.domain.auth.model.entity.AuthStateEntity;
import cn.glfs.chatgpt.data.domain.auth.model.valobj.AuthTypeVo;
import cn.glfs.chatgpt.data.domain.auth.service.IAuthService;
import cn.glfs.chatgpt.data.domain.weixin.model.entity.MessageTextEntity;
import cn.glfs.chatgpt.data.domain.weixin.model.entity.UserBehaviorMessageEntity;
import cn.glfs.chatgpt.data.domain.weixin.model.valobj.MsgTypeVO;
import cn.glfs.chatgpt.data.domain.weixin.service.IWeiXinBehaviorService;
import cn.glfs.chatgpt.data.types.common.Constants;
import cn.glfs.chatgpt.data.types.model.Response;
import cn.glfs.chatgpt.data.types.sdk.weixin.XmlUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 授权登录
 */

@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")//跨域
@RequestMapping("/api/${app.config.api-version}/auth/")
public class AuthController {
    @Resource
    private IAuthService authService;
    @Resource
    private IWeiXinBehaviorService weiXinBehaviorService;

    /**
     * 生成验证码，用于测试使用
     * <p>
     * curl -X POST \
     *  http://apix.natapp1.cc/api/v1/auth/gen/code \
     * -H 'Content-Type: application/x-www-form-urlencoded' \
     * -d 'openid=oxfA9w8-23yvwTmo2ombz0E4zJv4'
     *
     * curl -X POST \
     *  http://localhost:8091/api/v1/auth/gen/code \
     * -H 'Content-Type: application/x-www-form-urlencoded' \
     * -d 'openid=oxfA9w8-23yvwTmo2ombz0E4zJv4'
     */
    @RequestMapping(value = "gen/code", method = RequestMethod.POST)
    public Response<String> genCode() {
        String openid = "xfg";
        log.info("生成验证码开始，用户ID: {}", openid);
        try {
            UserBehaviorMessageEntity userBehaviorMessageEntity = new UserBehaviorMessageEntity();
            userBehaviorMessageEntity.setOpenId(openid);
            userBehaviorMessageEntity.setMsgType(MsgTypeVO.TEXT.getCode());
            userBehaviorMessageEntity.setContent("403");
            String xml = weiXinBehaviorService.acceptUserBehavior(userBehaviorMessageEntity);
            MessageTextEntity messageTextEntity = XmlUtil.xmlToBean(xml, MessageTextEntity.class);
            log.info("生成验证码完成，用户ID: {} 生成结果：{}", openid, messageTextEntity.getContent());
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(messageTextEntity.getContent())
                    .build();
        } catch (Exception e) {
            log.info("生成验证码失败，用户ID: {}", openid);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                    .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 【apix.natapp1.cc 是我在 <a href="https://natapp.cn/">https://natapp.cn</a> 购买的渠道，你需要自己购买一个使用】
     * 鉴权，根据鉴权结果返回 Token 码
     * curl -X POST \
     * http://apix.natapp1.cc/api/v1/auth/login \
     * -H 'Content-Type: application/x-www-form-urlencoded' \
     * -d 'code=7397'
     * <p>
     * curl -X POST \
     * http://localhost:8091/api/v1/auth/login \
     * -H 'Content-Type: application/x-www-form-urlencoded' \
     * -d 'code=8815'
     */
    @RequestMapping(value = "login",method = RequestMethod.POST)
    public Response<String> doLogin(@RequestParam String code){
        log.info("鉴权登录校验开始,验证码:{}",code);
        try{
            AuthStateEntity authStateEntity = authService.doLogin(code);
            log.info("鉴权登录校验完成,验证码:{},结果:{}",code, JSON.toJSONString(authStateEntity));
            //拦截,鉴权失败
            if(!AuthTypeVo.A0000.getCode().equals(authStateEntity.getCode())){
                return Response.<String>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            // 放行，鉴权成功
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(authStateEntity.getToken())
                    .build();
        }catch (Exception e){
            log.error("鉴权登录校验失败，验证码: {}", code);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }

    }

























}
