package com.example.photography.controller;

import com.example.photography.exception.GlobalExceptionHandler;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.UserRole;
import com.example.photography.repository.DepartmentRepository;
import com.example.photography.repository.UserRepository;
import com.example.photography.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AccountAdministrationControllerTest {

    private UserRepository userRepository;
    private RefreshTokenService refreshTokenService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        refreshTokenService = mock(RefreshTokenService.class);
        AccountAdministrationController controller = new AccountAdministrationController(
                userRepository,
                mock(DepartmentRepository.class),
                refreshTokenService
        );
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void assignsMultipleRolesAndRevokesExistingSessions() throws Exception {
        User user = user(7L);
        user.setTokenVersion(3);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        mockMvc.perform(put("/accounts/admin/7/role")
                        .contentType(APPLICATION_JSON)
                        .content("{\"roles\":[\"ADVISOR\",\"DIRECTOR\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(user.getRoles()).containsExactly(UserRole.DIRECTOR, UserRole.ADVISOR);
        assertThat(user.getEffectiveRoles()).containsExactly(UserRole.DIRECTOR);
        assertThat(user.getTokenVersion()).isEqualTo(4);
        verify(userRepository).save(user);
        verify(refreshTokenService).revokeAll(7L);
    }

    @Test
    void acceptsTheLegacySingleRoleRequestShape() throws Exception {
        User user = user(8L);
        when(userRepository.findById(8L)).thenReturn(Optional.of(user));

        mockMvc.perform(put("/accounts/admin/8/role")
                        .contentType(APPLICATION_JSON)
                        .content("{\"role\":\"MINISTER\"}"))
                .andExpect(status().isOk());

        assertThat(user.getRoles()).containsExactly(UserRole.MINISTER);
        verify(refreshTokenService).revokeAll(8L);
    }

    @Test
    void rejectsMissingEmptyNullAndLegacyAdminRoles() throws Exception {
        assertRejected("{}", "请至少选择一个角色");
        assertRejected("{\"roles\":[]}", "请至少选择一个角色");
        assertRejected("{\"roles\":[null]}", "角色不能为空");
        assertRejected("{\"roles\":[\"ADMIN\"]}", "请使用SUPER_ADMIN身份");

        verify(userRepository, never()).findById(7L);
        verify(refreshTokenService, never()).revokeAll(7L);
    }

    @Test
    void rejectsUnknownRoleAsBadRequest() throws Exception {
        mockMvc.perform(put("/accounts/admin/7/role")
                        .contentType(APPLICATION_JSON)
                        .content("{\"roles\":[\"UNKNOWN\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请求参数格式错误"));

        verify(userRepository, never()).findById(7L);
        verify(refreshTokenService, never()).revokeAll(7L);
    }

    private void assertRejected(String body, String message) throws Exception {
        mockMvc.perform(put("/accounts/admin/7/role")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(message));
    }

    private User user(Long id) {
        User user = new User("member-" + id, "password-123", "Member " + id, UserRole.MEMBER);
        user.setId(id);
        return user;
    }
}
