package tr.edu.duzce.mf.bm.cloudstorage.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.UserService;
import tr.edu.duzce.mf.bm.cloudstorage.util.JwtUtil;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        try {
            userService.registerUser(user);
            logger.info("Yeni kullanıcı kaydı başarılı: {}", user.getEmail());
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException | 
                 tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.UserAlreadyExistsException | 
                 tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.InvalidPasswordException e) {
            logger.warn("Kayıt hatası: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user); // Formun dolu kalması için nesneyi geri gönder
            return "register";
        } catch (Exception e) {
            logger.error("Beklenmedik kayıt hatası: {}", e.getMessage());
            model.addAttribute("error", "Sistemsel bir hata oluştu, lütfen daha sonra tekrar deneyiniz.");
            model.addAttribute("user", user);
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        @RequestParam(value = "rememberMe", required = false) boolean rememberMe,
                        HttpServletResponse response, Model model) {
        logger.debug("Giriş denemesi başlatıldı: {}, Hatırla Beni: {}", email, rememberMe);
        User user = userService.login(email, password);
        if (user != null) {
            long expiration = rememberMe ? JwtUtil.REMEMBER_ME_EXPIRATION : 3600000L; // 7 gün veya 1 saat
            String token = jwtUtil.generateToken(user, expiration);

            // JWT_TOKEN çerezini oluştur ve ekle
            Cookie cookie = new Cookie("JWT_TOKEN", token);
            cookie.setHttpOnly(true); // JavaScript tarafından erişilemez
            cookie.setPath("/");      // Tüm uygulama için geçerli
            cookie.setMaxAge((int) (expiration / 1000)); // Saniye cinsinden
            response.addCookie(cookie);

            logger.info("Kullanıcı girişi başarılı, çerez eklendi (Süre: {} saniye): {}", cookie.getMaxAge(), email);
            
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/dashboard";
        }

        logger.warn("Giriş başarısız (Hatalı şifre veya kullanıcı yok): {}", email);
        model.addAttribute("error", "Hatalı email veya şifre!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // JWT_TOKEN çerezini silmek için Max-Age 0 yapıyoruz
        Cookie cookie = new Cookie("JWT_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        logger.info("Kullanıcı oturumu kapatıldı (JWT çerezi silindi).");
        return "redirect:/login?logout=true";
    }
}