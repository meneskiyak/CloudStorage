package tr.edu.duzce.mf.bm.cloudstorage.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.AdminService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    private MockMvc mockMvc;
    @Mock private AdminService adminService;
    @InjectMocks private AdminController adminController;

    @BeforeEach
    void setup() { mockMvc = MockMvcBuilders.standaloneSetup(adminController).build(); }

    @Test
    void testUpdateLimit_ForbiddenForNormalUser() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User user = new User(); user.setRole("USER");
        session.setAttribute("loggedInUser", user);

        mockMvc.perform(post("/admin/updateLimit")
                        .param("email", "test@test.com")
                        .param("newLimitBytes", "1000")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testUpdateLimit_SuccessForAdmin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User admin = new User(); admin.setRole("ADMIN");
        session.setAttribute("loggedInUser", admin);

        mockMvc.perform(post("/admin/updateLimit")
                        .param("email", "test@test.com")
                        .param("newLimitBytes", "1000")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        Mockito.verify(adminService).updateUserUploadLimit("test@test.com", 1000L);
    }
}