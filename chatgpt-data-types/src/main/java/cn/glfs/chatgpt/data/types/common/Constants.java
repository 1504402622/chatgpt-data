package cn.glfs.chatgpt.data.types.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 状态码工具类
 */
public class Constants {
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public enum ResponseCode{
        SUCCESS("0000","成功"),
        UN_ERROR("0001", "未知失败"),
        ILLEGAL_PARAMETER("0002", "非法参数"),
        TOKEN_ERROR("0003", "权限拦截");

        private String code;
        private String info;
    }
}