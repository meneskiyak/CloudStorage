package tr.edu.duzce.mf.bm.cloudstorage.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tr.edu.duzce.mf.bm.cloudstorage.entity.User;
import tr.edu.duzce.mf.bm.cloudstorage.service.FileService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class FileControllerTest {

    private MockMvc mockMvc;
    @Mock private FileService fileService;
    @InjectMocks private FileController fileController;

    @BeforeEach
    void setup() { mockMvc = MockMvcBuilders.standaloneSetup(fileController).build(); }

    @Test
    void testUploadFile_Success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", new User());

        mockMvc.perform(post("/file/upload")
                        .param("fileName", "test.txt")
                        .param("fileSize", "100")
                        .param("mimeType", "text/plain")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        Mockito.verify(fileService, Mockito.times(1)).uploadFile(Mockito.any(), Mockito.any());
    }

    @Test
    void testDeleteFile() throws Exception {
        mockMvc.perform(post("/file/delete").param("fileId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        Mockito.verify(fileService).softDeleteFile(1L);
    }
}