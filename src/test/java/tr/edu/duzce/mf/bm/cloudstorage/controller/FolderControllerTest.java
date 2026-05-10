package tr.edu.duzce.mf.bm.cloudstorage.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.FolderService;
import tr.edu.duzce.mf.bm.cloudstorage.core.enums.Role;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.web.servlet.view.InternalResourceViewResolver;

@ExtendWith(MockitoExtension.class)
public class FolderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FolderService folderService;

    @InjectMocks
    private FolderController folderController;

    private User testUser;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/view/");
        viewResolver.setSuffix(".jsp");

        mockMvc = MockMvcBuilders.standaloneSetup(folderController)
                .setViewResolvers(viewResolver)
                .build();
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("Yeni klasör başarıyla oluşturulmalı")
    void shouldCreateFolder() throws Exception {
        // Klasör ismi ile POST yapıldığında klasör oluşturulmalı
        mockMvc.perform(post("/folder/create")
                        .param("name", "New Folder")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("success", "Klasör başarıyla oluşturuldu."));

        verify(folderService).createFolder(any());
    }

    @Test
    @DisplayName("Klasör adı güncellenmeli")
    void shouldRenameFolder() throws Exception {
        // Mevcut klasörün ismi değiştirilebilmeli
        mockMvc.perform(post("/folder/rename")
                        .param("folderId", "10")
                        .param("newName", "Renamed Folder")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("success", "Klasör adı güncellendi."));

        verify(folderService).renameFolder(eq(10L), eq("Renamed Folder"), eq(testUser));
    }

    @Test
    @DisplayName("Klasör çöpe taşınmalı")
    void shouldSoftDeleteFolder() throws Exception {
        // Klasör silindiğinde dashboard'a yönlendirmeli
        mockMvc.perform(post("/folder/delete")
                        .param("folderId", "10")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attribute("success", "Klasör çöpe taşındı."));

        verify(folderService).softDeleteFolder(eq(10L), eq(testUser));
    }

    @Test
    @DisplayName("Klasör geri yüklenmeli")
    void shouldRestoreFolder() throws Exception {
        // Çöp kutusundaki klasör geri alınabilmeli
        mockMvc.perform(post("/folder/restore")
                        .param("folderId", "10")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/trash"))
                .andExpect(flash().attribute("success", "Klasör geri yüklendi."));

        verify(folderService).restoreFolder(eq(10L), eq(testUser));
    }

    @Test
    @DisplayName("Klasör kalıcı olarak silinmeli")
    void shouldPermanentlyDeleteFolder() throws Exception {
        // Kalıcı silme işlemi sonrası çöp kutusuna dönmeli
        mockMvc.perform(post("/folder/delete-permanent")
                        .param("folderId", "10")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/trash"))
                .andExpect(flash().attribute("success", "Klasör kalıcı olarak silindi."));

        verify(folderService).permanentlyDeleteFolder(eq(10L), eq(testUser));
    }

    @Test
    @DisplayName("Klasör başka bir klasöre taşınmalı")
    void shouldMoveFolder() throws Exception {
        // Klasör taşıma işlemi doğrulanmalı
        mockMvc.perform(post("/folder/move")
                        .param("folderId", "10")
                        .param("targetFolderId", "20")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/dashboard?folderId=20"))
                .andExpect(flash().attribute("success", "Klasör başarıyla taşındı."));

        verify(folderService).moveFolder(eq(10L), any(), eq(testUser));
    }
}
