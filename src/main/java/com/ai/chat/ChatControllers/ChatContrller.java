package com.ai.chat.ChatControllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai.chat.Chatrepository.ChatRepository;
import com.ai.chat.Chatrepository.UserRepository;

import com.ai.chat.dto.chatRequuest;
import com.ai.chat.dto.ChatResponse;

import com.ai.chat.models.AppUser;
import com.ai.chat.models.ChatMessage;
import com.ai.chat.models.SarvamAiService;

@RestController
@RequestMapping("/api/chat")
public class ChatContrller {

    @Autowired
    private ChatRepository chatrepo;

    @Autowired
    private UserRepository user_repo;

    @Autowired
    private SarvamAiService sarvamAiService;

    // POST API
    @PostMapping("/response")
    public ChatResponse chat(@RequestBody chatRequuest request,
                             Principal principal) {

        // Find Logged-in User
        AppUser user = user_repo
                .findByUsername(principal.getName())
                .orElseThrow();

        // Get Chat History
        List<ChatMessage> history =
                chatrepo.findByUserOrderByCreatedAtAsc(user);

        // Save User Message
        ChatMessage userMsg = new ChatMessage();

        userMsg.setRole("user");
        userMsg.setContent(request.getMessage());
        userMsg.setUser(user);

        chatrepo.save(userMsg);

        // Get AI Response
        String ai_reply = sarvamAiService.askSarvam(
                history,
                request.getMessage()
        );

        // Save AI Message
        ChatMessage aiMsg = new ChatMessage();

        aiMsg.setRole("assistant");
        aiMsg.setContent(ai_reply);
        aiMsg.setUser(user);

        chatrepo.save(aiMsg);

        // Return Response
        return new ChatResponse(ai_reply);
    }

    // GET HISTORY API
    @GetMapping("/history")
    public List<ChatMessage> history(Principal principal) {

        AppUser user = user_repo
                .findByUsername(principal.getName())
                .orElseThrow();

        return chatrepo.findByUserOrderByCreatedAtAsc(user);
    }
}