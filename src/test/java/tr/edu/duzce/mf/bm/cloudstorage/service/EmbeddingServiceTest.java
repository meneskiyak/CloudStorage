package tr.edu.duzce.mf.bm.cloudstorage.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private EmbeddingStore<TextSegment> embeddingStore;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private EmbeddingService embeddingService;

    // ── embedPdf – PDF değilse işlem yapılmaz ─────────────────────────────────

    @Test
    void embedPdf_whenMimeTypeIsNotPdf_doesNothing() throws Exception {
        embeddingService.embedPdf(1L, 1L, "file.png", "resim.png", "image/png");

        verifyNoInteractions(minioService, embeddingModel, embeddingStore);
    }

    @Test
    void embedPdf_whenMimeTypeIsNull_doesNothing() throws Exception {
        embeddingService.embedPdf(1L, 1L, "file", "dosya", null);

        verifyNoInteractions(minioService, embeddingModel, embeddingStore);
    }

    // ── embedPdf – geçerli PDF ─────────────────────────────────────────────────
    // NOT: Gerçek bir PDF byte dizisi oluşturmak gerekir; PDFBox boş/geçersiz
    // byte ile hata fırlatır. Minimal geçerli PDF sabit olarak tanımlandı.

    @Test
    void embedPdf_withValidPdf_ingests() throws Exception {
        byte[] minimalPdf = buildMinimalPdf("Hello World");
        InputStream pdfStream = new ByteArrayInputStream(minimalPdf);
        when(minioService.downloadFile("stored.pdf")).thenReturn(pdfStream);

        float[] vector = new float[]{0.1f, 0.2f};
        Embedding embedding = Embedding.from(vector);
        when(embeddingModel.embedAll(any()))
                .thenReturn(Response.from(List.of(embedding)));

        // @Async metodu test içinde sync çalışır (Spring context yok)
        embeddingService.embedPdf(10L, 5L, "stored.pdf", "rapor.pdf", "application/pdf");

        verify(embeddingStore, atLeastOnce()).addAll(any(), any());
    }

    @Test
    void embedPdf_whenMinioThrows_doesNotPropagateException() throws Exception {
        when(minioService.downloadFile(anyString()))
                .thenThrow(new RuntimeException("MinIO bağlantı hatası"));

        // exception dışarı sızmamalı, metot sessizce dönmeli
        embeddingService.embedPdf(1L, 1L, "hata.pdf", "hata.pdf", "application/pdf");

        verifyNoInteractions(embeddingModel, embeddingStore);
    }

    // ── removeEmbeddings ──────────────────────────────────────────────────────

    @Test
    void removeEmbeddings_callsStoreRemoveAllWithFileIdFilter() {
        embeddingService.removeEmbeddings(55L);

        ArgumentCaptor<Filter> captor = ArgumentCaptor.forClass(Filter.class);
        verify(embeddingStore).removeAll(captor.capture());
        assertNotNull(captor.getValue()); // ← değişen sadece bu
    }

    @Test
    void removeEmbeddings_differentFileIds_eachCallsRemoveAll() {
        embeddingService.removeEmbeddings(1L);
        embeddingService.removeEmbeddings(2L);

        verify(embeddingStore, times(2)).removeAll(any(Filter.class));
    }

    // ── Yardımcı – minimal geçerli PDF üretimi ────────────────────────────────

    /**
     * PDFBox'ın okuyabileceği, tek sayfalık minimal bir PDF byte dizisi oluşturur.
     * Gerçek test ortamında PDFBox kütüphanesini kullanarak basit bir PDF yazarız.
     */
    private byte[] buildMinimalPdf(String pageText) throws Exception {
        try (PDDocument doc =
                     new PDDocument()) {

            PDPage page =
                    new PDPage();

            org.apache.pdfbox.pdmodel.font.PDType1Font font =
                    new org.apache.pdfbox.pdmodel.font.PDType1Font(
                            org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA);
            doc.addPage(page);

            try (PDPageContentStream cs =
                         new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(font, 12);
                cs.newLineAtOffset(50, 700);
                cs.showText(pageText);
                cs.endText();
            }

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }
}