package tr.edu.duzce.mf.bm.cloudstorage.service;

import io.minio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.StorageException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

    // MinioClient final class — Mockito 5 inline mocking ile mock'lanabilir.
    @Mock
    private MinioClient minioClient;

    private MinioService minioService;

    @BeforeEach
    void setUp() {
        minioService = new MinioService();
        // @Value alanlarını @PostConstruct tetiklemeden set ediyoruz.
        ReflectionTestUtils.setField(minioService, "bucket", "test-bucket");
        // minioClient'ı doğrudan inject ediyoruz; @PostConstruct'u atlatmak için
        // init() çağrılmıyor.
        ReflectionTestUtils.setField(minioService, "minioClient", minioClient);
    }

    // ── uploadFile ────────────────────────────────────────────────────────────

    @Test
    void uploadFile_returnsStoredName() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "rapor.pdf", "application/pdf", "pdf content".getBytes());

        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String storedName = minioService.uploadFile(file);

        assertNotNull(storedName);
        assertTrue(storedName.endsWith("_rapor.pdf"));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void uploadFile_stripsPathFromOriginalFilename() throws Exception {
        // Bazı OS/tarayıcılarda originalFilename path içerebilir.
        MockMultipartFile file = new MockMultipartFile(
                "file", "folder/sub/dosya.txt", "text/plain", "content".getBytes());

        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String storedName = minioService.uploadFile(file);

        // Path kısmı atılmış olmalı, sadece dosya adı kalmalı
        assertTrue(storedName.endsWith("_dosya.txt"));
        assertFalse(storedName.contains("/"));
    }

    @Test
    void uploadFile_whenMinioThrows_throwsStorageException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "hata.pdf", "application/pdf", "content".getBytes());

        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new RuntimeException("MinIO down"));

        StorageException ex = assertThrows(StorageException.class,
                () -> minioService.uploadFile(file));

        assertTrue(ex.getMessage().contains("hata.pdf"));
    }

    // ── downloadFile ──────────────────────────────────────────────────────────

    @Test
    void downloadFile_returnsInputStream() throws Exception {
        InputStream fakeStream = new ByteArrayInputStream("data".getBytes());
        // getObject dönüş tipi GetObjectResponse (InputStream alt sınıfı) —
        // mock null dönebilir çünkü sadece InputStream tipini kontrol ediyoruz.
        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenReturn(mock(GetObjectResponse.class));

        InputStream result = minioService.downloadFile("stored_file.pdf");

        assertNotNull(result);
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void downloadFile_whenMinioThrows_throwsStorageException() throws Exception {
        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(new RuntimeException("not found"));

        StorageException ex = assertThrows(StorageException.class,
                () -> minioService.downloadFile("missing.pdf"));

        assertTrue(ex.getMessage().contains("missing.pdf"));
    }

    // ── deleteFile ────────────────────────────────────────────────────────────

    @Test
    void deleteFile_callsRemoveObject() throws Exception {
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        minioService.deleteFile("to-delete.pdf");

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deleteFile_whenMinioThrows_throwsStorageException() throws Exception {
        doThrow(new RuntimeException("delete failed"))
                .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        StorageException ex = assertThrows(StorageException.class,
                () -> minioService.deleteFile("bad-file.pdf"));

        assertTrue(ex.getMessage().contains("bad-file.pdf"));
    }

    // ── copyFile ──────────────────────────────────────────────────────────────

    @Test
    void copyFile_callsCopyObject() throws Exception {
        when(minioClient.copyObject(any(CopyObjectArgs.class))).thenReturn(null);

        minioService.copyFile("source.pdf", "dest.pdf");

        verify(minioClient).copyObject(any(CopyObjectArgs.class));
    }

    @Test
    void copyFile_whenMinioThrows_throwsStorageException() throws Exception {
        when(minioClient.copyObject(any(CopyObjectArgs.class)))
                .thenThrow(new RuntimeException("copy failed"));

        StorageException ex = assertThrows(StorageException.class,
                () -> minioService.copyFile("source.pdf", "dest.pdf"));

        assertTrue(ex.getMessage().contains("source.pdf"));
    }
}