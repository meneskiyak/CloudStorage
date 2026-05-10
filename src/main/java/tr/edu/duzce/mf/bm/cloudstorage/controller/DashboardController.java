package tr.edu.duzce.mf.bm.cloudstorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.FileService;
import tr.edu.duzce.mf.bm.cloudstorage.service.FolderService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/search")
    @ResponseBody
    public Map<String, Object> searchAjax(@RequestParam(name = "q", required = false, defaultValue = "") String query,
                                          HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) request.getAttribute("currentUser");

        List<Map<String, Object>> folderResults = new ArrayList<>();
        List<Map<String, Object>> fileResults = new ArrayList<>();

        if (!query.trim().isEmpty()) {
            for (Folder f : folderService.searchFolders(currentUser, query)) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", f.getId());
                m.put("name", f.getName());
                folderResults.add(m);
            }
            for (FileItem f : fileService.searchUserFiles(currentUser, query, null)) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", f.getId());
                m.put("name", f.getOriginalName());
                m.put("mimeType", f.getMimeType());
                m.put("folderId", f.getFolder() != null ? f.getFolder().getId() : null);
                fileResults.add(m);
            }
        }

        result.put("folders", folderResults);
        result.put("files", fileResults);
        return result;
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

    @GetMapping("/settings")
    public String settings(HttpServletRequest request, Model model) {
        User currentUser = (User) request.getAttribute("currentUser");
        model.addAttribute("user", currentUser);
        model.addAttribute("activeNav", "settings");
        return "settings";
    }
}
