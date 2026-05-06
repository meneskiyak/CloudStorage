package tr.edu.duzce.mf.bm.cloudstorage.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.AdminService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/updateLimit")
    public String updateUserLimit(@RequestParam("email") String email,
                                  @RequestParam("newLimitBytes") Long newLimitBytes,
                                  HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        // Sadece Admin yetkisi olanlar yapabilir
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }

        try {
            adminService.updateUserUploadLimit(email, newLimitBytes);
        } catch (Exception e) {
            System.out.println("HATA: " + e.getMessage());
        }

        return "redirect:/dashboard";
    }
}