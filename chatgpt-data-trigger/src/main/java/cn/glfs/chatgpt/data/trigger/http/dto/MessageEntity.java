package cn.glfs.chatgpt.data.trigger.http.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 请求头的请求信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageEntity {
    private String role;
    private String content;
    private String name;
}
