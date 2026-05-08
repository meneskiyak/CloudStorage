package tr.edu.duzce.mf.bm.cloudstorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FolderAlreadyExistsException;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.FolderService;

import java.util.List;

@Controller
@RequestMapping("/folder")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @PostMapping("/create")
    public String createFolder(@RequestParam("name") String name,
                               @RequestParam(name = "parentId", required = false) Long parentId,
                               HttpServletRequest request,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        User currentUser = (User) request.getAttribute("currentUser");

        Folder parentFolder = null;
        if (parentId != null) {
            parentFolder = folderService.getFolder(parentId);
        }

        Folder newFolder = new Folder();
        newFolder.setName(name);
        newFolder.setParent(parentFolder);
        newFolder.setOwner(currentUser);

        try {
            folderService.createFolder(newFolder);
        } catch (FolderAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return parentId != null ? "redirect:/dashboard?folderId=" + parentId : "redirect:/dashboard";
    }

    @PostMapping("/upload")
    public String uploadFolder(@RequestParam("files") List<MultipartFile> files,
                               @RequestParam("relativePaths") List<String> relativePaths,
                               @RequestParam(name = "parentId", required = false) Long parentId,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {

        User currentUser = (User) request.getAttribute("currentUser");

        Folder parentFolder = null;
        if (parentId != null) {
            parentFolder = folderService.getFolder(parentId);
        }

        try {
            folderService.uploadFolder(files, relativePaths, parentFolder, currentUser);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return parentId != null ? "redirect:/dashboard?folderId=" + parentId : "redirect:/dashboard";
    }
}
