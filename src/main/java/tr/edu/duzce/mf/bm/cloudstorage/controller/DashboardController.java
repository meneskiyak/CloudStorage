package tr.edu.duzce.mf.bm.cloudstorage.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.FileService;
import tr.edu.duzce.mf.bm.cloudstorage.service.FolderService;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private FolderService folderService;

    @Autowired
    private FileService fileService;

    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam(value = "folderId", required = false) Long folderId,
                               HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        Folder currentFolder = null;
        if (folderId != null) {
            currentFolder = folderService.getFolderById(folderId);
            // Security check: ensure the folder belongs to the user
            if (currentFolder != null && !currentFolder.getOwner().getId().equals(user.getId())) {
                return "redirect:/dashboard";
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("currentFolder", currentFolder);
        model.addAttribute("folders", folderService.getSubFolders(currentFolder, user));
        model.addAttribute("files", fileService.getUserFiles(currentFolder, user));
        
        return "dashboard";
    }
}