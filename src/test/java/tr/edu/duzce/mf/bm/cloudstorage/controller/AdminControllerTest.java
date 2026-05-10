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
import tr.edu.duzce.mf.bm.cloudstorage.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/view/");
        viewResolver.setSuffix(".jsp");

        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setViewResolvers(viewResolver)
                .build();

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("Admin dashboard'a ADMIN yetkisiyle erişilmeli")
    void shouldShowAdminDashboard() throws Exception {
        // Admin rolündeki kullanıcı dashboard'u görebilmeli
        mockMvc.perform(get("/admin/dashboard")
                        .requestAttr("currentUser", adminUser))
                .andExpect(status().isOk())
                .andExpect(view().name("admin_dashboard"));
    }

    @Test
    @DisplayName("Admin dashboard'a yetkisiz erişim engellenmeli")
    void shouldRedirectRegularUserFromAdminDashboard() throws Exception {
        // Standart kullanıcı admin sayfasına girmeye çalıştığında login'e yönlendirilmeli
        mockMvc.perform(get("/admin/dashboard")
                        .requestAttr("currentUser", regularUser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?unauthorized=true"));
    }

    @Test
    @DisplayName("Sistem istatistikleri doğru yüklenmeli")
    void shouldShowStats() throws Exception {
        // Global istatistikler admin panelinde görüntülenmeli
        Map<String, Object> stats = new HashMap<>();
        stats.put("recentActivities", new ArrayList<>());
        when(userService.getGlobalStats()).thenReturn(stats);

        mockMvc.perform(get("/admin/stats")
                        .requestAttr("currentUser", adminUser))
                .andExpect(status().isOk())
                .andExpect(view().name("system_statistics"))
                .andExpect(model().attributeExists("stats"));
    }

    @Test
    @DisplayName("Kullanıcı kotası güncellenmeli")
    void shouldUpdateQuota() throws Exception {
        // Kullanıcının kotası admin tarafından değiştirilebilmeli
        User targetUser = new User();
        targetUser.setId(10L);
        when(userService.findById(10L)).thenReturn(targetUser);

        mockMvc.perform(post("/admin/users/update-quota")
                        .param("userId", "10")
                        .param("quotaGb", "5")
                        .requestAttr("currentUser", adminUser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin/users?success=quota"));
    }

    @Test
    @DisplayName("Kullanıcı rolü güncellenmeli")
    void shouldUpdateRole() throws Exception {
        // Kullanıcının rolü admin tarafından ID ile değiştirilebilmeli
        User targetUser = new User();
        targetUser.setId(10L);
        when(userService.findById(10L)).thenReturn(targetUser);

        mockMvc.perform(post("/admin/users/update-role")
                        .param("userId", "10")
                        .param("roleId", "0") // ADMIN ID
                        .requestAttr("currentUser", adminUser))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin/users?success=role"));
    }
}
