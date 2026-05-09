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
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.FileService;
import tr.edu.duzce.mf.bm.cloudstorage.service.MinioService;
import java.io.InputStream;

@Controller
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

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

        try {
            fileService.uploadFile(file, folderId, user);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return folderId == null ? "redirect:/dashboard" : "redirect:/dashboard?folderId=" + folderId;
    }

    @PostMapping("/delete")
    public String softDelete(@RequestParam("fileId") Long fileId,
                             @RequestParam(value = "folderId", required = false) Long folderId,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        User user = (User) request.getAttribute("currentUser");

        try {
            fileService.softDeleteFile(fileId, user);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return folderId == null ? "redirect:/dashboard" : "redirect:/dashboard?folderId=" + folderId;
    }

    @PostMapping("/rename")
    public String renameFile(@RequestParam("fileId") Long fileId,
                             @RequestParam("newName") String newName,
                             @RequestParam(value = "folderId", required = false) Long folderId,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        User user = (User) request.getAttribute("currentUser");

        try {
            fileService.renameFile(fileId, newName, user);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return folderId == null ? "redirect:/dashboard" : "redirect:/dashboard?folderId=" + folderId;
    }

    @PostMapping("/restore")
    public String restore(@RequestParam("fileId") Long fileId,
                          HttpServletRequest request,
                          RedirectAttributes redirectAttributes) {
        User user = (User) request.getAttribute("currentUser");
        try {
            fileService.restoreFile(fileId, user);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/trash";
    }

    @PostMapping("/delete-permanent")
    public String deletePermanent(@RequestParam("fileId") Long fileId,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        User user = (User) request.getAttribute("currentUser");
        try {
            fileService.permanentlyDeleteFile(fileId, user);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/trash";
    }

    @GetMapping("/preview")
    public void previewFile(@RequestParam("fileId") Long fileId,
                            HttpServletRequest request,
                            HttpServletResponse response) throws Exception {
        User user = (User) request.getAttribute("currentUser");
        FileItem file = fileService.getFileById(fileId, user);
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
                             HttpServletResponse response) throws Exception {
        User user = (User) request.getAttribute("currentUser");
        FileItem file = fileService.getFileById(fileId, user);
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
        try {
            fileService.toggleStar(fileId, user);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        if ("starred".equals(redirect)) return "redirect:/starred";
        return folderId == null ? "redirect:/dashboard" : "redirect:/dashboard?folderId=" + folderId;
    }
}
