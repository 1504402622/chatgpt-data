package cn.glfs.chatgpt.data.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模型枚举类
 */
@Getter
@AllArgsConstructor
public enum ChatGLMModel {

    GLM_4_Air("glm-4-air"),
    GLM_4_FlashX("glm-4-flashx"),
    ;
    private final String code;

    public static ChatGLMModel get(String code){
        switch (code){
            case "glm-4-air":
                return ChatGLMModel.GLM_4_Air;
            case "glm-4-flashx":
                return ChatGLMModel.GLM_4_FlashX;
            default:
                return ChatGLMModel.GLM_4_Air;
        }
    }
}
