package tr.edu.duzce.mf.bm.cloudstorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/ingest")
    @ResponseBody
    public String ingest(@RequestParam("text") String text) {
        chatService.ingestText(text);
        return "Belge Qdrant'a eklendi.";
    }

    @PostMapping("/ask")
    @ResponseBody
    public String askPost(@RequestParam("question") String question, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return chatService.ask(question, currentUser.getId());
    }
}
