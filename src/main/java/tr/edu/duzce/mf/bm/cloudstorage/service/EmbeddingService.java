package tr.edu.duzce.mf.bm.cloudstorage.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.filter.Filter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Service
public class EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);

    @Autowired
    @Qualifier("indexEmbeddingModel")
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private MinioService minioService;

    private final DocumentSplitter splitter = new DocumentByCharacterSplitter(1000, 0);
    @Async
    public void embedPdf(Long fileId,
                         Long userId,
                         String storedName,
                         String originalName,
                         String mimeType) {
        if (!"application/pdf".equalsIgnoreCase(mimeType)) {
            return;
        }

        logger.info("PDF dosyası vektörleştiriliyor: {}", originalName);

        try (InputStream inputStream = minioService.downloadFile(storedName)) {
            byte[] bytes = inputStream.readAllBytes();

            String text;
            try (PDDocument pdDocument = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                text = stripper.getText(pdDocument);
            }

            Document document = Document.from(text);
            document.metadata().put("userId", userId);
            document.metadata().put("fileId", fileId);
            document.metadata().put("filename", originalName);

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(splitter)
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            ingestor.ingest(document);
            logger.info("PDF dosyası başarıyla vektörleştirildi: {}", originalName);
        } catch (Exception e) {
            logger.error("PDF vektörleştirme hatası: {}", originalName, e);
        }
    }

    public void removeEmbeddings(Long fileId) {
        logger.info("Dosya vektörleri siliniyor, fileId: {}", fileId);
        Filter fileIdFilter = metadataKey("fileId").isEqualTo(fileId);
        embeddingStore.removeAll(fileIdFilter);
    }
}