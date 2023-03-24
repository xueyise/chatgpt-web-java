package com.hncboy.chatgpt.handler.emitter;

import cn.hutool.core.date.DateUtil;
import com.hncboy.chatgpt.api.ChatClientUtil;
import com.hncboy.chatgpt.api.listener.ConsoleStreamListener;
import com.hncboy.chatgpt.api.listener.ParsedEventSourceListener;
import com.hncboy.chatgpt.api.listener.ResponseBodyEmitterStreamListener;
import com.hncboy.chatgpt.api.parser.ChatCompletionResponseParser;
import com.hncboy.chatgpt.domain.request.ChatProcessRequest;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.Arrays;

/**
 * @author hncboy
 * @date 2023/3/24 15:51
 * ApiKey 响应处理
 */
@Component
public class ApiKeyResponseEmitter implements ResponseEmitter {

    @Override
    public ResponseBodyEmitter requestToResponseEmitter(ChatProcessRequest chatProcessRequest) {
        // 系统消息
        Message systemMessage = Message.builder()
                .role(Message.Role.SYSTEM)
                .content("You are ChatGPT, a large language model trained by OpenAI. Answer as concisely as possible.\\nKnowledge cutoff: 2021-09-01\\nCurrent date: ".concat(DateUtil.today()))
                .build();
        // 用户消息
        Message userMessage = Message.builder().role(Message.Role.USER)
                .content(chatProcessRequest.getPrompt())
                .build();

        ChatCompletion chatCompletion = ChatCompletion.builder()
                .messages(Arrays.asList(systemMessage, userMessage))
                .build();

        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        // 构建事件监听器
        ParsedEventSourceListener parsedEventSourceListener = new ParsedEventSourceListener.Builder()
                .addListener(new ConsoleStreamListener())
                .addListener(new ResponseBodyEmitterStreamListener(emitter))
                .setParser(new ChatCompletionResponseParser())
                .build();

        ChatClientUtil.buildOpenAiStreamClient().streamChatCompletion(chatCompletion, parsedEventSourceListener);
        return emitter;
    }
}