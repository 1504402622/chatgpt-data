package cn.glfs.chatcpt.data.trigger.http;


import cn.glfs.chatcpt.data.trigger.http.dto.ChatGPTRequestDTO;
import cn.glfs.chatgpt.data.types.exception.ChatGPTException;
import com.alibaba.fastjson.JSON;
import com.glfs.chatgpt.common.Constants;
import com.glfs.chatgpt.domain.chatModel.ChatChoice;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionRequest;
import com.glfs.chatgpt.domain.chatModel.ChatCompletionResponse;
import com.glfs.chatgpt.domain.chatModel.Message;
import com.glfs.chatgpt.session.OpenAiSession;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@RestController()
@CrossOrigin("*")//表示该接口允许所有来源的跨域请求
@RequestMapping("/api/v0/")
public class ChatGPTAIServiceControllerOld {

    @Resource//对象注入不依赖于 Spring 框架
    private OpenAiSession openAiSession;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 流式问题，ChatGPT 请求接口
     * <p>
     * curl -X POST \
     * http://localhost:8080/api/v1/chat/completions \
     * -H 'Content-Type: application/json;charset=utf-8' \
     * -H 'Authorization: b8b6' \
     * -d '{
     * "messages": [
     * {
     * "content": "写一个java冒泡排序",
     * "role": "user"
     * }
     * ],
     * "model": "gpt-3.5-turbo"
     * }'
     */
    @RequestMapping(value = "chat/completions", method = RequestMethod.POST)
    //ResponseBodyEmitter是 Spring Framework 中用于异步推送数据到客户端的类，适用于需要实时更新数据或需要异步处理请求的情况
    public ResponseBodyEmitter completionsStream(@RequestBody ChatGPTRequestDTO request, @RequestHeader("Authorization") String token, HttpServletResponse response){
        log.info("流式问答请求开始，使用模型：{} 请求信息：{}", request.getModel(), JSON.toJSONString(request.getMessages()));
        try {
            //1.基础配置:流式输出、编码、禁用缓存
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            //Cache-Control 是 HTTP 头中的一个重要属性，用于控制缓存行为，包括指定缓存策略、缓存过期时间、缓存验证方式,“no-cache”，表示禁用缓存。
            response.setHeader("Cache-Control", "no-cache");

            //2.token验证
            if (!token.equals("6666")) throw new RuntimeException("token err");

            //3.异步处理HTTP响应处理类(并设置超时时间)
            ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
            //用了emitter.onCompletion()方法来注册一个回调函数，当ResponseBodyEmitter中的数据流全部发送完成时，这个回调函数就会被执行。
            emitter.onCompletion(() -> {
                log.info("流式问答请求完成，使用模型：{}", request.getModel());
            });
            //emitter.onError()方法用于注册一个回调函数，用于处理在流式响应处理过程中发生的异常情况。具体来说，当在流式问答请求处理过程中出现异常时，该回调函数会被触发，
            emitter.onError(throwable -> log.error("流式问答请求异常，使用模型：{}", request.getModel(), throwable));

            //4.构建并封装请求
            List<Message> messages = request.getMessages().stream()
                    .map(entiyt -> Message.builder()
                            .role(Constants.Role.valueOf(entiyt.getRole().toUpperCase()))
                            .content(entiyt.getContent())
                            .name(entiyt.getName())
                            .build())
                    .collect(Collectors.toList());

            ChatCompletionRequest completionRequest = ChatCompletionRequest
                    .builder()
                    .stream(true)
                    .messages(messages)
                    .model(ChatCompletionRequest.Model.GPT_3_5_TURBO.getCode())
                    .build();

            //5.进行流式传输
            openAiSession.chatCompletions(completionRequest, new EventSourceListener() {
                @Override
                public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                    //fastjson 库:json-》java对象
                    ChatCompletionResponse chatCompletionResponse = JSON.parseObject(data, ChatCompletionResponse.class);
                    List<ChatChoice> chatChoices = chatCompletionResponse.getChoices();
                    for (ChatChoice chatChoice : chatChoices) {
                        Message delta = chatChoice.getDelta();
                        //如果是助手问的就不返回回答，可能是一开始声明角色的前提
                        if (Constants.Role.ASSISTANT.getCode().equals(delta.getRole())) continue;

                        //应答完成
                        //获取结束原因且原因等于停止”stop“就停止输出
                        String finishReason = chatChoice.getFinishReason();
                        if (StringUtils.isNoneBlank(finishReason) && "stop".equals(finishReason)) {
                            emitter.complete();
                            break;
                        }

                        // 发送信息
                        try {
                            emitter.send(delta.getContent());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            return emitter;
        }catch (Exception e){
            log.error("流式应答，请求模型：{} 发生异常", request.getModel(), e);
            throw new ChatGPTException(e.getMessage());
        }
    }


    /**
     * 应该是测试线程池的消息发送功能吧
     * @param response
     * @return
     */
    @RequestMapping(value = "/chat", method = RequestMethod.GET)
    public ResponseBodyEmitter completionsStream(HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        //创建一个ResponseBodyEmitter对象emitter，用于向客户端发送实时的事件流（Server-Sent Events）。
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();

        //异步处理
        threadPoolExecutor.execute(()->{
            for (int i = 0; i < 10; i++) {
                try {
                    //实际上用于向正在连接到该ResponseBodyEmitter的客户端发送消息，如果没连接就不发送是吧
                    emitter.send("strdddddddddddddddd\r\n" + i);
                    Thread.sleep(100);
                }catch (Exception e){
                    throw new RuntimeException(e);
                }
                emitter.complete();
            }
        });
        return emitter;
    }

}
