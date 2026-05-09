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

    @PostMapping("/rename")
    public String renameFolder(@RequestParam("folderId") Long folderId,
                               @RequestParam("newName") String newName,
                               @RequestParam(value = "parentId", required = false) Long parentId,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {

        User user = (User) request.getAttribute("currentUser");

        try {
            folderService.renameFolder(folderId, newName, user);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return parentId != null ? "redirect:/dashboard?folderId=" + parentId : "redirect:/dashboard";
    }

    @PostMapping("/delete")
    public String softDelete(@RequestParam("folderId") Long folderId,
                             @RequestParam(value = "parentId", required = false) Long parentId,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        User user = (User) request.getAttribute("currentUser");

        try {
            folderService.softDeleteFolder(folderId, user);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return parentId != null ? "redirect:/dashboard?folderId=" + parentId : "redirect:/dashboard";
    }

    @PostMapping("/restore")
    public String restore(@RequestParam("folderId") Long folderId,
                          HttpServletRequest request,
                          RedirectAttributes redirectAttributes) {
        User user = (User) request.getAttribute("currentUser");
        try {
            folderService.restoreFolder(folderId, user);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/trash";
    }

    @PostMapping("/delete-permanent")
    public String deletePermanent(@RequestParam("folderId") Long folderId,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        User user = (User) request.getAttribute("currentUser");
        try {
            folderService.permanentlyDeleteFolder(folderId, user);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/trash";
    }

    @PostMapping("/star")
    public String toggleStar(@RequestParam("folderId") Long folderId,
                             @RequestParam(value = "parentId", required = false) Long parentId,
                             @RequestParam(value = "redirect", defaultValue = "dashboard") String redirect,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        User user = (User) request.getAttribute("currentUser");
        try {
            folderService.toggleStar(folderId, user);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        if ("starred".equals(redirect)) return "redirect:/starred";
        return parentId != null ? "redirect:/dashboard?folderId=" + parentId : "redirect:/dashboard";
    }
}
