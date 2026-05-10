package tr.edu.duzce.mf.bm.cloudstorage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.UserService;

import java.util.List;

/**
 * Admin işlemlerini yürüten controller.
 * Sadece ADMIN rolüne sahip kullanıcılar erişebilir.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String showAdminDashboard(@RequestAttribute(value = "currentUser", required = false) User user, Model model) {
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login?unauthorized=true";
        }
        model.addAttribute("user", user); // _sidebar.jsp için 'user' gerekli
        return "admin_dashboard";
    }

    @GetMapping("/stats")
    public String showStats(@RequestAttribute(value = "currentUser", required = false) User user, Model model) {
        if (user == null || !"ADMIN".equals(user.getRole())) return "redirect:/login";
        
        java.util.Map<String, Object> stats = userService.getGlobalStats();
        
        // Aktiviteler için "ne kadar süre önce" bilgisini ekleyelim
        List<tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem> activities = (List<tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem>) stats.get("recentActivities");
        java.util.List<String> timeAgos = new java.util.ArrayList<>();
        for (tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem item : activities) {
            timeAgos.add(userService.getTimeAgo(item.getUpdatedAt()));
        }
        model.addAttribute("timeAgos", timeAgos);
        
        model.addAttribute("user", user);
        model.addAttribute("stats", stats);
        return "system_statistics";
    }

    @GetMapping("/users")
    public String listUsers(@RequestAttribute(value = "currentUser", required = false) User user, Model model) {
        if (user == null || !"ADMIN".equals(user.getRole())) return "redirect:/login";
        
        model.addAttribute("user", user);
        List<User> allUsers = userService.findAllUsers();
        model.addAttribute("users", allUsers);
        return "user_management";
    }

    @PostMapping("/users/update-role")
    public String updateRole(@RequestParam("userId") Long userId, @RequestParam("role") String role) {
        User targetUser = userService.findById(userId);
        if (targetUser != null) {
            targetUser.setRole(role);
            userService.updateUser(targetUser);
        }
        return "redirect:/admin/users?success=role";
    }

    @PostMapping("/users/update-quota")
    public String updateQuota(@RequestParam("userId") Long userId, @RequestParam("quotaGb") Long quotaGb) {
        User targetUser = userService.findById(userId);
        if (targetUser != null) {
            // GB'ı Byte'a çevir (1 GB = 1024 * 1024 * 1024 Byte)
            targetUser.setUploadLimitBytes(quotaGb * 1073741824L);
            userService.updateUser(targetUser);
        }
        return "redirect:/admin/users?success=quota";
    }
}
