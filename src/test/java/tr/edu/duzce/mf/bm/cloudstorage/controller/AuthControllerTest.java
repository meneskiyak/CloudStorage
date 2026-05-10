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
import tr.edu.duzce.mf.bm.cloudstorage.service.UserService;
import tr.edu.duzce.mf.bm.cloudstorage.util.JwtUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.web.servlet.view.InternalResourceViewResolver;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/view/");
        viewResolver.setSuffix(".jsp");

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    @DisplayName("Ana sayfa dashboard'a yönlendirmeli")
    void shouldRedirectToDashboard() throws Exception {
        // Root URL'ye gelindiğinde dashboard'a yönlendirme yapılmalı
        mockMvc.perform(get("/"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("Giriş sayfası görüntülenmeli")
    void shouldShowLoginPage() throws Exception {
        // /login GET isteği login.jsp sayfasını dönmeli
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("Başarılı giriş işlemi ve JWT çerezi")
    void shouldLoginSuccessfully() throws Exception {
        // Doğru bilgilerle giriş yapıldığında JWT oluşturulmalı ve dashboard'a yönlendirilmeli
        User user = new User();
        user.setEmail("test@example.com");
        
        when(userService.login("test@example.com", "password")).thenReturn(user);
        when(jwtUtil.generateToken(any(), anyLong())).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/login")
                        .param("email", "test@example.com")
                        .param("password", "password")
                        .param("rememberMe", "false"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(cookie().exists("JWT_TOKEN"));
    }

    @Test
    @DisplayName("Kayıt sayfası görüntülenmeli")
    void shouldShowRegisterPage() throws Exception {
        // /register GET isteği register.jsp sayfasını dönmeli
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    @DisplayName("Yeni kullanıcı kaydı başarılı olmalı")
    void shouldRegisterSuccessfully() throws Exception {
        // Kullanıcı bilgileri gönderildiğinde kayıt yapılmalı ve login'e yönlendirilmeli
        mockMvc.perform(post("/register")
                        .param("email", "new@example.com")
                        .param("passwordHash", "Password123!"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?registered=true"));

        verify(userService).registerUser(any(User.class));
    }

    @Test
    @DisplayName("Oturum kapatma JWT çerezini silmeli")
    void shouldLogoutSuccessfully() throws Exception {
        // Logout isteği JWT çerezini sıfırlamalı ve login'e yönlendirmeli
        mockMvc.perform(get("/logout"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?logout=true"))
                .andExpect(cookie().maxAge("JWT_TOKEN", 0));
    }
}
