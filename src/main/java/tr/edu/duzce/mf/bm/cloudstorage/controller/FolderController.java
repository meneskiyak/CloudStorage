package tr.edu.duzce.mf.bm.cloudstorage.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.FolderService;

@Controller
@RequestMapping("/folder")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @PostMapping("/create")
    public String createFolder(@RequestParam("name") String name,
                               @RequestParam(value = "parentId", required = false) Long parentId,
                               HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        Folder newFolder = new Folder();
        newFolder.setName(name);
        newFolder.setOwner(user);

        if (parentId != null) {
            Folder parent = new Folder();
            parent.setId(parentId);
            newFolder.setParent(parent);
        }

        folderService.createFolder(newFolder);
        return parentId == null ? "redirect:/dashboard" : "redirect:/dashboard?folderId=" + parentId;
    }
}