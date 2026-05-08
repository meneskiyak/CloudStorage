package tr.edu.duzce.mf.bm.cloudstorage.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.FileService;

@Controller
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "folderId", required = false) Long folderId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Lütfen bir dosya seçin.");
            return folderId == null ? "redirect:/dashboard" : "redirect:/dashboard?folderId=" + folderId;
        }

        try {
            fileService.uploadFile(file, folderId, user);
            session.setAttribute("loggedInUser", user);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return folderId == null ? "redirect:/dashboard" : "redirect:/dashboard?folderId=" + folderId;
    }

    @PostMapping("/delete")
    public String softDelete(@RequestParam("fileId") Long fileId,
                             @RequestParam(value = "folderId", required = false) Long folderId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        try {
            fileService.softDeleteFile(fileId, user);
            session.setAttribute("loggedInUser", user);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return folderId == null ? "redirect:/dashboard" : "redirect:/dashboard?folderId=" + folderId;
    }
}
