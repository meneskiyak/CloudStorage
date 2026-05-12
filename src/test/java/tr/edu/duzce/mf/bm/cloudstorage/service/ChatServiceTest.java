package tr.edu.duzce.mf.bm.cloudstorage.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private EmbeddingStore<TextSegment> embeddingStore;

    @Mock
    private ChatModel chatModel;

    @InjectMocks
    private ChatService chatService;

    // ── ask – eşleşme bulunamadı ──────────────────────────────────────────────

    @Test
    void ask_whenNoMatchesFound_returnsNoInfoMessage() {
        float[] vector = new float[]{0.1f, 0.2f};
        Embedding questionEmbedding = Embedding.from(vector);

        when(embeddingModel.embed(anyString()))
                .thenReturn(Response.from(questionEmbedding));

        EmbeddingSearchResult<TextSegment> emptyResult = mock(EmbeddingSearchResult.class);
        when(emptyResult.matches()).thenReturn(Collections.emptyList());
        when(embeddingStore.search(any(EmbeddingSearchRequest.class)))
                .thenReturn(emptyResult);

        String result = chatService.ask("bulunamayan konu", 1L);

        assertEquals("Bu konuda belgelerinizde bilgi bulunamad\u0131.", result);
        verifyNoInteractions(chatModel);
    }

    // ── ask – eşleşme bulundu ─────────────────────────────────────────────────

    @Test
    void ask_whenMatchesFound_buildContextAndCallsChatModel() {
        float[] vector = new float[]{0.5f, 0.5f};
        Embedding questionEmbedding = Embedding.from(vector);

        when(embeddingModel.embed(anyString()))
                .thenReturn(Response.from(questionEmbedding));

        TextSegment segment = TextSegment.from("Java bir programlama dilidir.");
        EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.9, "id-1", questionEmbedding, segment);

        EmbeddingSearchResult<TextSegment> searchResult = mock(EmbeddingSearchResult.class);
        when(searchResult.matches()).thenReturn(List.of(match));
        when(embeddingStore.search(any(EmbeddingSearchRequest.class)))
                .thenReturn(searchResult);

        AiMessage aiMessage = AiMessage.from("Java, nesne yonelimli bir dildir.");
        ChatResponse chatResponse = mock(ChatResponse.class);
        when(chatResponse.aiMessage()).thenReturn(aiMessage);
        when(chatModel.chat(any(dev.langchain4j.data.message.UserMessage.class)))
                .thenReturn(chatResponse);

        String result = chatService.ask("Java nedir?", 10L);

        assertEquals("Java, nesne yonelimli bir dildir.", result);
    }

    // ── ask – userId filtresi doğru iletiliyor mu? ────────────────────────────

    @Test
    void ask_searchRequest_containsExpectedParameters() {
        float[] vector = new float[]{0.1f};
        Embedding emb = Embedding.from(vector);
        when(embeddingModel.embed(anyString())).thenReturn(Response.from(emb));

        EmbeddingSearchResult<TextSegment> emptyResult = mock(EmbeddingSearchResult.class);
        when(emptyResult.matches()).thenReturn(Collections.emptyList());

        ArgumentCaptor<EmbeddingSearchRequest> captor =
                ArgumentCaptor.forClass(EmbeddingSearchRequest.class);
        when(embeddingStore.search(captor.capture())).thenReturn(emptyResult);

        chatService.ask("soru", 99L);

        EmbeddingSearchRequest captured = captor.getValue();
        assertEquals(5, captured.maxResults());
        assertEquals(0.7, captured.minScore());
        assertNotNull(captured.filter());
    }

    // ── ask – exception fırlatılırsa hata mesajı döner ───────────────────────

    @Test
    void ask_whenEmbeddingModelThrows_returnsErrorMessage() {
        when(embeddingModel.embed(anyString()))
                .thenThrow(new RuntimeException("embedding servisi coktü"));

        String result = chatService.ask("herhangi bir soru", 1L);

        assertTrue(result.startsWith("Hata:"));
        assertTrue(result.contains("embedding servisi coktü"));
    }
}