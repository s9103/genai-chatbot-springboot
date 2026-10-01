package com.sharad.chatbot;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.sharad.chatbot.Dto.*;

@RestController @RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chat;
    public ChatController(ChatService chat) { this.chat = chat; }

    @PostMapping
    public ChatRes send(@RequestAttribute("userId") Long userId, @Valid @RequestBody ChatReq r) {
        return new ChatRes(chat.send(userId, r.message().trim()));
    }

    @GetMapping("/history")
    public List<MsgView> history(@RequestAttribute("userId") Long userId) { return chat.history(userId); }
}
