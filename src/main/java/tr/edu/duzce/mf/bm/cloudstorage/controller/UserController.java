package tr.edu.duzce.mf.bm.cloudstorage.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.UserService;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    // 1. Kayıt Sayfasını Ekrana Getir (GET)
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // WEB-INF/view/register.jsp dosyasını arar
    }

    // 2. Formdan Gelen Bilgilerle Kayıt Ol (POST)
    @PostMapping("/register")
    public String processRegister(@ModelAttribute User user, Model model) {
        try {
            // Service katmanına gönder, o da veritabanına kaydetsin
            userService.registerUser(user);
            return "redirect:/login?registered=true"; // Başarılıysa login'e yönlendir
        } catch (Exception e) {
            model.addAttribute("error", "Kayıt olurken bir hata oluştu!");
            return "register"; // Hata varsa kayıt sayfasına geri dön
        }
    }

    // 3. Giriş Sayfasını Ekrana Getir (GET)
    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // WEB-INF/view/login.jsp dosyasını arar
    }

    // 4. Formdan Gelen Bilgilerle Giriş Yap (POST)
    @PostMapping("/login")
    public String processLogin(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               HttpSession session,
                               Model model) {

        // Service katmanında email ve şifre kontrolü yap
        User user = userService.login(email, password);

        if (user != null) {
            // Başarılı! Kullanıcıyı oturuma (Session) kaydet
            session.setAttribute("loggedInUser", user);
            return "redirect:/"; // Dosya yükleme ana sayfasına (Dashboard) yönlendir
        } else {
            // Başarısız! Hata mesajı ile login sayfasına geri dön
            model.addAttribute("error", "Hatalı email veya şifre girdiniz!");
            return "login";
        }
    }

    // 5. Çıkış Yap (Logout)
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Session'ı tamamen temizle
        return "redirect:/login?logout=true";
    }
}