package cn.glfs.chatgpt.data.domain.auth.service;

import cn.glfs.chatgpt.data.domain.auth.model.entity.AuthStateEntity;
import cn.glfs.chatgpt.data.domain.auth.model.valobj.AuthTypeVo;
import com.google.common.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 鉴权服务（验证用户的验证码信息）
 */
@Slf4j
@Service
public class AuthService extends AbstractAuthService{
    @Resource
    private Cache<String, String> codeCache;

    @Override
    protected AuthStateEntity checkCode(String code) {
        //code 是键，openId 是对应的值
        String openId = codeCache.getIfPresent(code);//通过验证码信息找用户id
        //如果缓存中没有用户
        if(StringUtils.isBlank(openId)){
            log.info("鉴权,用户收入的验证码不存在{}",code);
            return AuthStateEntity.builder()
                    .code(AuthTypeVo.A0001.getCode())
                    .info(AuthTypeVo.A0001.getInfo())
                    .build();
        }
        //校验成功移除缓存Key值(因为存了双向的映射)
        codeCache.invalidate(openId);
        codeCache.invalidate(code);
        //成功
        return AuthStateEntity.builder()
                .code(AuthTypeVo.A0000.getCode())
                .info(AuthTypeVo.A0000.getInfo())
                .openId(openId)
                .build();
    }

    @Override
    public boolean checkToken(String token) {
        return isVerify(token);
    }
}
