package tr.edu.duzce.mf.bm.cloudstorage.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import tr.edu.duzce.mf.bm.cloudstorage.core.exceptions.FolderAlreadyExistsException;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.FolderService;

@Controller
@RequestMapping("/folder")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @GetMapping
    public String listFolders(@RequestParam(name = "parentId", required = false) Long parentId,
                              HttpSession session,
                              Model model) {
        User currentUser = (User) session.getAttribute("loggedInUser");

        Folder parentFolder = null;
        if (parentId != null) {
            parentFolder = folderService.getFolder(parentId);
        }

        List<Folder> folders = folderService.getSubFolders(parentFolder, currentUser);

        model.addAttribute("folders", folders);
        model.addAttribute("currentFolder", parentFolder);
        model.addAttribute("newFolder", new Folder());

        return "folders";
    }

    @PostMapping("/create")
    public String createFolder(@RequestParam("name") String name,
                               @RequestParam(name = "parentId", required = false) Long parentId,
                               HttpSession session,
                               Model model) {

        User currentUser = (User) session.getAttribute("loggedInUser");

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
            model.addAttribute("error", e.getMessage());
            model.addAttribute("folders", folderService.getSubFolders(parentFolder, currentUser));
            model.addAttribute("currentFolder", parentFolder);
            model.addAttribute("newFolder", new Folder());
            return "folders";
        }

        return parentId != null ? "redirect:/dashboard?folderId=" + parentId : "redirect:/dashboard";
    }
}