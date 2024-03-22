package cn.glfs.chatgpt.data.trigger.http.dto;

import cn.glfs.chatgpt.data.types.enums.ChatGPTModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 请求头
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGPTRequestDTO {

    /** 默认模型 */
    private String model = ChatGPTModel.GPT_3_5_TURBO.getCode();

    /** 问题描述 */
    private List<MessageEntity> messages;

}