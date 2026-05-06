package tr.edu.duzce.mf.bm.cloudstorage.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login"; // Giriş yapmayanları login'e atar
        }

        model.addAttribute("user", user);
        return "dashboard"; // dashboard.jsp sayfasını açar
    }
}