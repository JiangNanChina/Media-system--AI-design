package com.example.photography.controller;

import com.example.photography.service.JoinApplicationService;
import com.example.photography.service.MediaSubmissionService;
import com.example.photography.service.VerificationRequestLimiter;
import com.example.photography.security.JwtAuthenticationFilter;
import com.example.photography.security.MaintenanceModeFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({MediaSubmissionController.class, JoinApplicationController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(PermanentDeletionControllerTest.MethodSecurityConfig.class)
class PermanentDeletionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MediaSubmissionService mediaSubmissionService;

    @MockBean
    private JoinApplicationService joinApplicationService;

    @MockBean
    private VerificationRequestLimiter verificationRequestLimiter;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private MaintenanceModeFilter maintenanceModeFilter;

    @Test
    @WithMockUser(username = "root", roles = "SUPER_ADMIN")
    void superAdminCanPermanentlyDeleteSubmission() throws Exception {
        mockMvc.perform(delete("/submissions/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("视频投稿及服务器文件已永久删除"));

        verify(mediaSubmissionService).deletePermanently(7L);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void legacyAdminCanPermanentlyDeleteJoinApplication() throws Exception {
        mockMvc.perform(delete("/join-applications/8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("入部申请及服务器作品文件已永久删除"));

        verify(joinApplicationService).deletePermanently(8L);
    }

    @Test
    @WithMockUser(username = "minister", roles = "MINISTER")
    void ministerCannotPermanentlyDeleteSubmissionOrJoinApplication() throws Exception {
        mockMvc.perform(delete("/submissions/7"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/join-applications/8"))
                .andExpect(status().isForbidden());

        verify(mediaSubmissionService, never()).deletePermanently(7L);
        verify(joinApplicationService, never()).deletePermanently(8L);
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }
}
