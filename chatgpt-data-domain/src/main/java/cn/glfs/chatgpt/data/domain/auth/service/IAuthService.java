package cn.glfs.chatgpt.data.domain.auth.service;

import cn.glfs.chatgpt.data.domain.auth.model.entity.AuthStateEntity;

/**
 * 鉴权验证服务接口
 */
public interface IAuthService {
    /**
     * 登录验证
     * @param code 验证码
     * @return Token
     */
    AuthStateEntity doLogin(String code);

    boolean checkToken(String token);
}
