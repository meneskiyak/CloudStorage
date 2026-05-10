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

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user) {
        userService.registerUser(user);
        logger.info("Yeni kullanıcı kaydı başarılı: {}", user.getEmail());
        return "redirect:/login?registered=true";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        @RequestParam(value = "rememberMe", required = false) boolean rememberMe,
                        HttpServletResponse response) {
        logger.debug("Giriş denemesi başlatıldı: {}, Hatırla Beni: {}", email, rememberMe);
        User user = userService.login(email, password);
        
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