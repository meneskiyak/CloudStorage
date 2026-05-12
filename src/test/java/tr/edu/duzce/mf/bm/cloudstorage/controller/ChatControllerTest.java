package tr.edu.duzce.mf.bm.cloudstorage.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.ChatService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
    }

    // ── /api/chat/ingest ──────────────────────────────────────────────────────

    @Test
    void ingest_callsServiceAndReturnsSuccessMessage() throws Exception {
        doNothing().when(chatService).ingestText("hello world");

        mockMvc.perform(post("/api/chat/ingest")
                        .param("text", "hello world"))
                .andExpect(status().isOk())
                .andExpect(content().string("Belge Qdrant'a eklendi."));

        verify(chatService).ingestText("hello world");
    }

    @Test
    void ingest_withEmptyText_stillDelegatesToService() throws Exception {
        doNothing().when(chatService).ingestText("");

        mockMvc.perform(post("/api/chat/ingest")
                        .param("text", ""))
                .andExpect(status().isOk());

        verify(chatService).ingestText("");
    }

    // ── /api/chat/ask ─────────────────────────────────────────────────────────

    @Test
    void askPost_returnsServiceAnswer() throws Exception {
        User user = new User();
        user.setId(42L);

        when(chatService.ask("what is spring?", 42L)).thenReturn("Spring is a framework.");

        mockMvc.perform(post("/api/chat/ask")
                        .param("question", "what is spring?")
                        .requestAttr("currentUser", user))
                .andExpect(status().isOk())
                .andExpect(content().string("Spring is a framework."));

        verify(chatService).ask("what is spring?", 42L);
    }

    @Test
    void askPost_serviceReturnsNoInfoMessage_passedThrough() throws Exception {
        User user = new User();
        user.setId(7L);

        when(chatService.ask("unknown topic", 7L))
                .thenReturn("No information found.");

        mockMvc.perform(post("/api/chat/ask")
                        .param("question", "unknown topic")
                        .requestAttr("currentUser", user))
                .andExpect(status().isOk())
                .andExpect(content().string("No information found."));
    }
}