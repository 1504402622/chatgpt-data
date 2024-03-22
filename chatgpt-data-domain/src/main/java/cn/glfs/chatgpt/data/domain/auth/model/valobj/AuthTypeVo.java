package cn.glfs.chatgpt.data.domain.auth.model.valobj;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 验证结果响应码
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum AuthTypeVo {

    A0000("0000","验证成功"),
    A0001("0001","验证码不存在"),
    A0002("0002","验证码无效");

    private String code;
    private String info;//错误信息

}
