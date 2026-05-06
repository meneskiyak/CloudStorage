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
import tr.edu.duzce.mf.bm.cloudstorage.service.FolderService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class FolderControllerTest {

    private MockMvc mockMvc;
    @Mock private FolderService folderService;
    @InjectMocks private FolderController folderController;

    @BeforeEach
    void setup() { mockMvc = MockMvcBuilders.standaloneSetup(folderController).build(); }

    @Test
    void testCreateFolder_Success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", new User());

        mockMvc.perform(post("/folder/create")
                        .param("name", "Fotograflar")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        Mockito.verify(folderService).createFolder(Mockito.any());
    }
}