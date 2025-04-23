package cn.glfs.chatgpt.data.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模型枚举类
 */
@Getter
@AllArgsConstructor
public enum DeepSeekModel {

    DeepSeek_V3("deepseek-chat"),
    DeepSeek_R1("deepseek-reasoner"),
    ;
    private final String code;

}
