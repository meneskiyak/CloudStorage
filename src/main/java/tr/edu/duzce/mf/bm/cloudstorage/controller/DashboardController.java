package tr.edu.duzce.mf.bm.cloudstorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard(@RequestAttribute(value = "currentUser", required = false) User user, Model model) {
        if (user == null) {
            return "redirect:/login"; 
        }

        model.addAttribute("user", user);
        return "dashboard"; // dashboard.jsp sayfasını açar
    }
}