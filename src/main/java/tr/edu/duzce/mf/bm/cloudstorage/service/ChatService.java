package tr.edu.duzce.mf.bm.cloudstorage.service;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.filter.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ChatModel chatModel;

    @Autowired
    public ChatService(@Qualifier("indexEmbeddingModel") EmbeddingModel embeddingModel,
                       EmbeddingStore<TextSegment> embeddingStore,
                       ChatModel chatModel) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.chatModel = chatModel;
    }

    public void ingestText(String text) {
        Document document = Document.from(text);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 50))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(document);
    }

    public String ask(String question, Long userId) {
        try {
            Embedding questionEmbedding = embeddingModel.embed(question).content();

            Filter userFilter = metadataKey("userId").isEqualTo(userId);

            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(questionEmbedding)
                    .maxResults(5)
                    .minScore(0.7)
                    .filter(userFilter)
                    .build();

            List<EmbeddingMatch<TextSegment>> matches = embeddingStore
                    .search(searchRequest)
                    .matches();

            logger.info("RAG soru: '{}' | userId: {} | bulunan eşleşme sayısı: {}", question, userId, matches.size());
            matches.forEach(m -> logger.info("  -> skor: {} | metin: {}", m.score(), m.embedded().text().substring(0, Math.min(100, m.embedded().text().length()))));

            if (matches.isEmpty()) {
                return "Bu konuda belgelerinizde bilgi bulunamadı.";
            }

            String context = matches.stream()
                    .map(m -> m.embedded().text())
                    .collect(Collectors.joining("\n---\n"));

            String prompt = String.format("""
                    Aşağıdaki bağlamı kullanarak soruyu yanıtla.
                    Bağlamda yoksa "Bilmiyorum" de.
                    
                    Bağlam:
                    %s
                    
                    Soru: %s
                    """, context, question);

            return chatModel.chat(UserMessage.from(prompt))
                    .aiMessage()
                    .text();

        } catch (Exception e) {
            logger.error("RAG soru hatası: {}", e.getMessage(), e);
            return "Hata: " + e.getMessage();
        }
    }
}