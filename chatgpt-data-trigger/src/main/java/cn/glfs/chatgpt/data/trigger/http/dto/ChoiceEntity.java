package cn.glfs.chatgpt.data.trigger.http.dto;

import cn.glfs.chatgpt.data.domain.openai.model.entity.MessageEntity;
import lombok.Data;

/**
 * 响应结果的返回信息
 */
@Data
public class ChoiceEntity {

    /** stream = true 请求参数里返回的属性是 delta */
    private MessageEntity delta;
    /** stream = true 请求参数里返回的属性是 message */
    private MessageEntity message;
}
