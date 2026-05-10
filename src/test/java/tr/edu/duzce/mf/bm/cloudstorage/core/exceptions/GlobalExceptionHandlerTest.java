package tr.edu.duzce.mf.bm.cloudstorage.core.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("FileNotFoundException durumunda 404 sayfasına yönlendirmeli")
    void shouldHandleNotFoundException() throws Exception {
        // Kaynak bulunamadığında error/404 view'ı dönmeli
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));
    }

    @Test
    @DisplayName("AccessDeniedException durumunda 403 sayfasına yönlendirmeli")
    void shouldHandleAccessDeniedException() throws Exception {
        // Yetkisiz erişimde error/403 view'ı dönmeli
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));
    }

    @Test
    @DisplayName("StorageQuotaExceededException durumunda dashboard'a kota hatasıyla yönlendirmeli")
    void shouldHandleQuotaExceededException() throws Exception {
        // Kota dolduğunda dashboard'a error=quota parametresiyle yönlendirmeli
        mockMvc.perform(get("/test/quota-exceeded"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/dashboard?error=quota"));
    }

    @Test
    @DisplayName("UserNotFoundException durumunda login'e hata ile yönlendirmeli")
    void shouldHandleUserNotFoundException() throws Exception {
        // Giriş hatalarında login sayfasına yönlendirme yapılmalı
        mockMvc.perform(get("/test/user-not-found"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login?error=invalid"));
    }

    @Test
    @DisplayName("Genel hata durumunda 500 sayfasına yönlendirmeli")
    void shouldHandleGeneralException() throws Exception {
        // Beklenmedik hatalarda error/500 view'ı dönmeli
        mockMvc.perform(get("/test/error"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/500"));
    }

    /**
     * İstisnaları tetiklemek için kullanılan geçici Controller.
     */
    @Controller
    static class TestController {
        @GetMapping("/test/not-found")
        public void throwNotFound() { throw new FileNotFoundException("Bulunamadı"); }

        @GetMapping("/test/access-denied")
        public void throwAccessDenied() { throw new AccessDeniedException("Yetki yok"); }

        @GetMapping("/test/quota-exceeded")
        public void throwQuotaExceeded() { throw new StorageQuotaExceededException("Kota doldu"); }

        @GetMapping("/test/user-not-found")
        public void throwUserNotFound() { throw new UserNotFoundException("Kullanıcı yok"); }

        @GetMapping("/test/error")
        public void throwError() throws Exception { throw new Exception("Genel hata"); }
    }
}
