package tr.edu.duzce.mf.bm.cloudstorage.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.FileService;
import tr.edu.duzce.mf.bm.cloudstorage.service.FolderService;
import tr.edu.duzce.mf.bm.cloudstorage.service.MinioService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.web.servlet.view.InternalResourceViewResolver;

@ExtendWith(MockitoExtension.class)
public class FileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FileService fileService;

    @Mock
    private FolderService folderService;

    @Mock
    private MinioService minioService;

    @InjectMocks
    private FileController fileController;

    private User testUser;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/view/");
        viewResolver.setSuffix(".jsp");

        mockMvc = MockMvcBuilders.standaloneSetup(fileController)
                .setViewResolvers(viewResolver)
                .build();
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
    }

    @Test
    @DisplayName("Dosya başarıyla yüklenmeli")
    void shouldUploadFile() throws Exception {
        // Dosya yüklendiğinde dashboard'a yönlendirmeli ve başarı mesajı vermeli
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        mockMvc.perform(multipart("/file/upload")
                        .file(file)
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("success", "Dosya başarıyla yüklendi."));

        verify(fileService).uploadFile(any(), any(), eq(testUser));
    }

    @Test
    @DisplayName("Boş dosya yükleme hatası")
    void shouldErrorOnEmptyFileUpload() throws Exception {
        // Dosya seçilmeden yükleme yapılırsa hata mesajı vermeli
        MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        mockMvc.perform(multipart("/file/upload")
                        .file(file)
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("error", "Lütfen bir dosya seçin."));
    }

    @Test
    @DisplayName("Dosya çöpe taşınmalı")
    void shouldSoftDeleteFile() throws Exception {
        // Dosya ID ile çöpe taşınmalı
        mockMvc.perform(post("/file/delete")
                        .param("fileId", "100")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("success", "Dosya çöpe taşındı."));

        verify(fileService).softDeleteFile(eq(100L), eq(testUser));
    }

    @Test
    @DisplayName("Dosya adı güncellenmeli")
    void shouldRenameFile() throws Exception {
        // Yeni dosya adı ile güncelleme yapılmalı
        mockMvc.perform(post("/file/rename")
                        .param("fileId", "100")
                        .param("newName", "new_name.txt")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("success", "Dosya adı güncellendi."));

        verify(fileService).renameFile(eq(100L), eq("new_name.txt"), eq(testUser));
    }

    @Test
    @DisplayName("Dosya kalıcı olarak silinmeli")
    void shouldPermanentlyDeleteFile() throws Exception {
        // Çöp kutusundaki dosya kalıcı olarak silinebilmeli
        mockMvc.perform(post("/file/delete-permanent")
                        .param("fileId", "100")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/trash"))
                .andExpect(flash().attribute("success", "Dosya kalıcı olarak silindi."));

        verify(fileService).permanentlyDeleteFile(eq(100L), eq(testUser));
    }

    @Test
    @DisplayName("Yıldız durumu değiştirilmeli")
    void shouldToggleStar() throws Exception {
        // Dosya favorilere eklenebilmeli veya çıkarılabilmeli
        mockMvc.perform(post("/file/star")
                        .param("fileId", "100")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/dashboard"));

        verify(fileService).toggleStar(eq(100L), eq(testUser));
    }
}
