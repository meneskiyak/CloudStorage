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
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import tr.edu.duzce.mf.bm.cloudstorage.core.enums.Role;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.FileService;
import tr.edu.duzce.mf.bm.cloudstorage.service.FolderService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FolderService folderService;

    @Mock
    private FileService fileService;

    @InjectMocks
    private DashboardController dashboardController;

    private User testUser;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/view/");
        viewResolver.setSuffix(".jsp");

        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController)
                .setViewResolvers(viewResolver)
                .build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("Dashboard ana sayfası doğru verilerle yüklenmeli")
    void shouldShowDashboard() throws Exception {
        // Dashboard açıldığında kullanıcıya ait klasör ve dosyalar listelenmeli
        when(folderService.getSubFolders(any(), any())).thenReturn(Collections.emptyList());
        when(fileService.getUserFiles(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("folders", "files", "user"))
                .andExpect(model().attribute("activeNav", "home"));
    }

    @Test
    @DisplayName("Çöp kutusu sayfası yüklenmeli")
    void shouldShowTrash() throws Exception {
        // Çöp kutusu açıldığında silinen öğeler listelenmeli
        when(folderService.getDeletedFolders(any())).thenReturn(Collections.emptyList());
        when(fileService.getDeletedFiles(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/trash")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isOk())
                .andExpect(view().name("trash"))
                .andExpect(model().attribute("activeNav", "trash"));
    }

    @Test
    @DisplayName("Yıldızlı öğeler sayfası yüklenmeli")
    void shouldShowStarred() throws Exception {
        // Yıldızlı öğeler sayfası favori dosya ve klasörleri göstermeli
        when(folderService.getStarredFolders(any())).thenReturn(Collections.emptyList());
        when(fileService.getStarredFiles(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/starred")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isOk())
                .andExpect(view().name("starred"))
                .andExpect(model().attribute("activeNav", "starred"));
    }

    @Test
    @DisplayName("Arama işlemi AJAX ile sonuç dönmeli")
    void shouldReturnSearchResults() throws Exception {
        // Arama yapıldığında JSON formatında klasör ve dosya sonuçları dönmeli
        when(folderService.searchFolders(any(), anyString())).thenReturn(Collections.emptyList());
        when(fileService.searchUserFiles(any(), anyString(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/search")
                        .param("q", "test")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folders").isArray())
                .andExpect(jsonPath("$.files").isArray());
    }

    @Test
    @DisplayName("Son dosyalar sayfası yüklenmeli")
    void shouldShowRecent() throws Exception {
        // En son işlem yapılan öğeler sayfası yüklenmeli
        when(folderService.getRecentFolders(any())).thenReturn(Collections.emptyList());
        when(fileService.getRecentFiles(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/recent")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isOk())
                .andExpect(view().name("recent"))
                .andExpect(model().attribute("activeNav", "recent"));
    }

    @Test
    @DisplayName("Ayarlar sayfası yüklenmeli")
    void shouldShowSettings() throws Exception {
        // Kullanıcı ayarları sayfası yüklenmeli
        mockMvc.perform(get("/settings")
                        .requestAttr("currentUser", testUser))
                .andExpect(status().isOk())
                .andExpect(view().name("settings"))
                .andExpect(model().attribute("activeNav", "settings"));
    }
}
