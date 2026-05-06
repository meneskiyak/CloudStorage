package tr.edu.duzce.mf.bm.cloudstorage.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.FileService;

import java.util.UUID;

@Controller
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("fileName") String fileName,
                             @RequestParam("fileSize") Long fileSize,
                             @RequestParam("mimeType") String mimeType,
                             @RequestParam(value = "folderId", required = false) Long folderId,
                             HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        FileItem file = new FileItem();
        file.setOriginalName(fileName);
        file.setStoredName(UUID.randomUUID().toString()); // Diskteki eşsiz ismi
        file.setStoragePath("/storage/path/mock");
        file.setFileSizeBytes(fileSize);
        file.setMimeType(mimeType);
        file.setOwner(user);

        if (folderId != null) {
            Folder folder = new Folder();
            folder.setId(folderId);
            file.setFolder(folder);
        }

        try {
            fileService.uploadFile(file, user); // Kota kontrolü serviste yapılıyor
            session.setAttribute("loggedInUser", user); // Kotayı güncelleyip session'a yansıt
        } catch (Exception e) {
            // İleride arkadaşın arayüze error basmak için burayı model.addAttribute ile süsleyebilir
            System.out.println("HATA: " + e.getMessage());
        }

        return folderId == null ? "redirect:/dashboard" : "redirect:/dashboard?folderId=" + folderId;
    }

    @PostMapping("/delete")
    public String softDelete(@RequestParam("fileId") Long fileId) {
        fileService.softDeleteFile(fileId);
        return "redirect:/dashboard";
    }
}