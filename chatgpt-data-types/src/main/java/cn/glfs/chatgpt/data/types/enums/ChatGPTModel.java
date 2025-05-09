package cn.glfs.chatgpt.data.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模型枚举类
 */
@Getter
@AllArgsConstructor
public enum ChatGPTModel {

    GPT_3_5_TURBO("gpt-3.5-turbo"),
    GPT_4_TURBO("gpt-4"),
    ;
    private final String code;

}
