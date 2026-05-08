package com.teacher.management.controller;

import com.teacher.management.common.Result;
import com.teacher.management.dto.ChatRequestDTO;
import com.teacher.management.service.ChatService;
import com.teacher.management.vo.ChatResponseVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Result<ChatResponseVO> chat(@RequestBody ChatRequestDTO requestDTO) {
        try {
            String question = requestDTO == null ? null : requestDTO.getMessage();
            String answer = chatService.chat(question);
            return Result.success(new ChatResponseVO(question, answer));
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error("聊天接口调用失败: " + e.getMessage());
        }
    }
}
