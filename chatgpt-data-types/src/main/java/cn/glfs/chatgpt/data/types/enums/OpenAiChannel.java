package cn.glfs.chatgpt.data.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OpenAiChannel {

    ChatGLM("ChatGLM"),
    ChatGPT("ChatGPT"),
    DeepSeek("DeepSeek")

    ;
    private final String code;

    public static OpenAiChannel getChannel(String model) {
        if (model.toLowerCase().contains("gpt") || model.toLowerCase().contains("dall")) return OpenAiChannel.ChatGPT;
        if (model.toLowerCase().contains("glm") || model.toLowerCase().contains("cogview")) return OpenAiChannel.ChatGLM;
        if (model.toLowerCase().contains("deepseek")) return OpenAiChannel.DeepSeek;
        return null;
    }

}
