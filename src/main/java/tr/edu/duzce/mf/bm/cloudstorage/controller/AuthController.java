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
    public String register(@ModelAttribute User user) {
        userService.registerUser(user);
        return "redirect:/login?registered=true";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        HttpServletResponse response, Model model) {
        User user = userService.login(email, password);
        if (user != null) {
            String token = jwtUtil.generateToken(user);

            // JWT_TOKEN çerezini oluştur ve ekle
            Cookie cookie = new Cookie("JWT_TOKEN", token);
            cookie.setHttpOnly(true); // JavaScript tarafından erişilemez
            cookie.setPath("/");      // Tüm uygulama için geçerli
            cookie.setMaxAge(3600);   // 1 saat (JwtUtil ile uyumlu)
            response.addCookie(cookie);

            logger.info("Kullanıcı girişi başarılı, JWT üretildi: {}", email);
            return "redirect:/dashboard";
        }

        logger.warn("Hatalı giriş denemesi: {}", email);
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