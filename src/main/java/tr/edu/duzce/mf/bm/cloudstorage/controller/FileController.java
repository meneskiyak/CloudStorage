package tr.edu.duzce.mf.bm.cloudstorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tr.edu.duzce.mf.bm.cloudstorage.entity.FileItem;
import tr.edu.duzce.mf.bm.cloudstorage.entity.Folder;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.FileService;
import tr.edu.duzce.mf.bm.cloudstorage.service.FolderService;
import tr.edu.duzce.mf.bm.cloudstorage.service.MinioService;
import java.io.InputStream;

@Controller
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private MinioService minioService;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "folderId", required = false) Long folderId,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        User user = (User) request.getAttribute("currentUser");

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Lütfen bir dosya seçin.");
            return folderId == null ? "redirect:/dashboard" : "redirect:/dashboard?folderId=" + folderId;
        }

        fileService.uploadFile(file, folderId, user);
        redirectAttributes.addFlashAttribute("success", "Dosya başarıyla yüklendi.");

        return folderId == null ? "redirect:/dashboard" : "redirect:/dashboard?folderId=" + folderId;
    }

    @PostMapping("/delete")
    public String softDelete(@RequestParam("fileId") Long fileId,
                             @RequestParam(value = "folderId", required = false) Long folderId,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        User user = (User) request.getAttribute("currentUser");

        fileService.softDeleteFile(fileId, user);
        redirectAttributes.addFlashAttribute("success", "Dosya çöpe taşındı.");

        return folderId == null ? "redirect:/dashboard" : "redirect:/dashboard?folderId=" + folderId;
    }

    @PostMapping("/rename")
    public String renameFile(@RequestParam("fileId") Long fileId,
                             @RequestParam("newName") String newName,
                             @RequestParam(value = "folderId", required = false) Long folderId,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        User user = (User) request.getAttribute("currentUser");

        fileService.renameFile(fileId, newName, user);
        redirectAttributes.addFlashAttribute("success", "Dosya adı güncellendi.");

        return folderId == null ? "redirect:/dashboard" : "redirect:/dashboard?folderId=" + folderId;
    }

    @PostMapping("/restore")
    public String restore(@RequestParam("fileId") Long fileId,
                          HttpServletRequest request,
                          RedirectAttributes redirectAttributes) {
        User user = (User) request.getAttribute("currentUser");
        fileService.restoreFile(fileId, user);
        redirectAttributes.addFlashAttribute("success", "Dosya geri yüklendi.");
        return "redirect:/trash";
    }

    @PostMapping("/delete-permanent")
    public String deletePermanent(@RequestParam("fileId") Long fileId,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        User user = (User) request.getAttribute("currentUser");
        fileService.permanentlyDeleteFile(fileId, user);
        redirectAttributes.addFlashAttribute("success", "Dosya kalıcı olarak silindi.");
        return "redirect:/trash";
    }

    @GetMapping("/preview")
    public void previewFile(@RequestParam("fileId") Long fileId,
                            HttpServletRequest request,
                            HttpServletResponse response) throws java.io.IOException {
        User user = (User) request.getAttribute("currentUser");
        FileItem file = fileService.getFileById(fileId, user);

        fileService.touchFile(fileId, user); // Son kullanılanlara ekle/güncelle

        if (!file.canPreview()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        response.setContentType(file.getMimeType());
        response.setHeader("Content-Disposition", "inline; filename=\"" + file.getOriginalName() + "\"");
        response.setContentLengthLong(file.getFileSizeBytes());
        try (InputStream in = minioService.downloadFile(file.getStoredName())) {
            StreamUtils.copy(in, response.getOutputStream());
        }
    }

    @GetMapping("/download")
    public void downloadFile(@RequestParam("fileId") Long fileId,
                             HttpServletRequest request,
                             HttpServletResponse response) throws java.io.IOException {
        User user = (User) request.getAttribute("currentUser");
        FileItem file = fileService.getFileById(fileId, user);

        fileService.touchFile(fileId, user); // Son kullanılanlara ekle/güncelle

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getOriginalName() + "\"");
        response.setContentLengthLong(file.getFileSizeBytes());
        try (InputStream in = minioService.downloadFile(file.getStoredName())) {
            StreamUtils.copy(in, response.getOutputStream());
        }
    }

    @PostMapping("/star")
    public String toggleStar(@RequestParam("fileId") Long fileId,
                             @RequestParam(value = "folderId", required = false) Long folderId,
                             @RequestParam(value = "redirect", defaultValue = "dashboard") String redirect,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        User user = (User) request.getAttribute("currentUser");
        fileService.toggleStar(fileId, user);

        if ("starred".equals(redirect)) return "redirect:/starred";
        return folderId == null ? "redirect:/dashboard" : "redirect:/dashboard?folderId=" + folderId;
    }

    @PostMapping("/move")
    public String moveFile(@RequestParam("fileId") Long fileId,
                           @RequestParam(value = "targetFolderId", required = false) Long targetFolderId,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        User user = (User) request.getAttribute("currentUser");
        Folder targetFolder = targetFolderId != null ? folderService.getFolder(targetFolderId) : null;
        fileService.moveFile(fileId, targetFolder, user);
        redirectAttributes.addFlashAttribute("success", "Dosya başarıyla taşındı.");
        return "redirect:/dashboard" + (targetFolderId != null ? "?folderId=" + targetFolderId : "");
    }
}
