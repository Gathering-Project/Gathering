package nbc_final.gathering.domain.chatting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChattingController {

    @PostMapping("/send")
    public String sendMessage(@RequestParam String senderId,
                              @RequestParam String receiverId,
                              @RequestParam String content) {
        chatMessageService.sendMessage(senderId, receiverId, content);
        return "Message sent from " + senderId + " to " + receiverId;
}