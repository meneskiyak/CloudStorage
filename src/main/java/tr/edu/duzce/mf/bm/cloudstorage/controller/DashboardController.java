package tr.edu.duzce.mf.bm.cloudstorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.FileService;
import tr.edu.duzce.mf.bm.cloudstorage.service.FolderService;

@Controller
public class DashboardController {

    @Autowired
    private FolderService folderService;

    @Autowired
    private FileService fileService;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(name = "folderId", required = false) Long folderId,
                            HttpServletRequest request,
                            Model model) {

        User currentUser = (User) request.getAttribute("currentUser");

        Folder currentFolder = null;
        if (folderId != null) {
            currentFolder = folderService.getFolder(folderId);
        }

        model.addAttribute("folders", folderService.getSubFolders(currentFolder, currentUser));
        model.addAttribute("files", fileService.getUserFiles(currentFolder, currentUser));
        model.addAttribute("currentFolder", currentFolder);
        model.addAttribute("user", currentUser);
        model.addAttribute("activeNav", "home");

        return "dashboard";
    }

    @GetMapping("/trash")
    public String trash(HttpServletRequest request, Model model) {
        User currentUser = (User) request.getAttribute("currentUser");

        model.addAttribute("folders", folderService.getDeletedFolders(currentUser));
        model.addAttribute("files", fileService.getDeletedFiles(currentUser));
        model.addAttribute("user", currentUser);
        model.addAttribute("activeNav", "trash");

        return "trash";
    }

    @GetMapping("/starred")
    public String starred(HttpServletRequest request, Model model) {
        User currentUser = (User) request.getAttribute("currentUser");

        model.addAttribute("folders", folderService.getStarredFolders(currentUser));
        model.addAttribute("files", fileService.getStarredFiles(currentUser));
        model.addAttribute("user", currentUser);
        model.addAttribute("activeNav", "starred");

        return "starred";
    }
}
