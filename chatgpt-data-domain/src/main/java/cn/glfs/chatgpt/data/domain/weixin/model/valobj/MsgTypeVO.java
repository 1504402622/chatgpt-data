package cn.glfs.chatgpt.data.domain.weixin.model.valobj;


import lombok.*;

/**
 * 声明微信公众号消息类型值对象，用于描述对象属性的值，为值对象。(具体看官方)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum MsgTypeVO{
    EVENT("event","事件消息"),
    TEXT("text","文本消息");

    private String code;
    private String desc;

}
