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

@Controller
public class DashboardController {

    @Autowired
    private FolderService folderService;

    @Autowired
    private FileService fileService;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(name = "folderId", required = false) Long folderId,
                            HttpSession session,
                            Model model) {

        User currentUser = (User) session.getAttribute("loggedInUser");

        Folder currentFolder = null;
        if (folderId != null) {
            currentFolder = folderService.getFolder(folderId);
        }

        model.addAttribute("folders",
                folderService.getSubFolders(currentFolder, currentUser));
        model.addAttribute("files",
                fileService.getUserFiles(currentFolder, currentUser));
        model.addAttribute("currentFolder", currentFolder);
        model.addAttribute("user", currentUser);

        return "dashboard";
    }
}