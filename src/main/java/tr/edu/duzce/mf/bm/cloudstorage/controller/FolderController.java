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

        folderService.createFolder(newFolder);
        redirectAttributes.addFlashAttribute("success", "Klasör başarıyla oluşturuldu.");

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

        folderService.uploadFolder(files, relativePaths, parentFolder, currentUser);
        redirectAttributes.addFlashAttribute("success", "Klasör başarıyla yüklendi.");

        return parentId != null ? "redirect:/dashboard?folderId=" + parentId : "redirect:/dashboard";
    }

    @PostMapping("/rename")
    public String renameFolder(@RequestParam("folderId") Long folderId,
                               @RequestParam("newName") String newName,
                               @RequestParam(value = "parentId", required = false) Long parentId,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {

        User user = (User) request.getAttribute("currentUser");

        folderService.renameFolder(folderId, newName, user);
        redirectAttributes.addFlashAttribute("success", "Klasör adı güncellendi.");

        return parentId != null ? "redirect:/dashboard?folderId=" + parentId : "redirect:/dashboard";
    }

    @PostMapping("/delete")
    public String softDelete(@RequestParam("folderId") Long folderId,
                             @RequestParam(value = "parentId", required = false) Long parentId,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        User user = (User) request.getAttribute("currentUser");

        folderService.softDeleteFolder(folderId, user);
        redirectAttributes.addFlashAttribute("success", "Klasör çöpe taşındı.");

        return parentId != null ? "redirect:/dashboard?folderId=" + parentId : "redirect:/dashboard";
    }

    @PostMapping("/restore")
    public String restore(@RequestParam("folderId") Long folderId,
                          HttpServletRequest request,
                          RedirectAttributes redirectAttributes) {
        User user = (User) request.getAttribute("currentUser");
        folderService.restoreFolder(folderId, user);
        redirectAttributes.addFlashAttribute("success", "Klasör geri yüklendi.");
        return "redirect:/trash";
    }

    @PostMapping("/delete-permanent")
    public String deletePermanent(@RequestParam("folderId") Long folderId,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        User user = (User) request.getAttribute("currentUser");
        folderService.permanentlyDeleteFolder(folderId, user);
        redirectAttributes.addFlashAttribute("success", "Klasör kalıcı olarak silindi.");
        return "redirect:/trash";
    }

    @PostMapping("/star")
    public String toggleStar(@RequestParam("folderId") Long folderId,
                             @RequestParam(value = "parentId", required = false) Long parentId,
                             @RequestParam(value = "redirect", defaultValue = "dashboard") String redirect,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        User user = (User) request.getAttribute("currentUser");
        folderService.toggleStar(folderId, user);

        if ("starred".equals(redirect)) return "redirect:/starred";
        return parentId != null ? "redirect:/dashboard?folderId=" + parentId : "redirect:/dashboard";
    }
}
