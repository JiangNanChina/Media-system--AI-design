package com.example.photography.security;

import com.example.photography.model.entity.User;
import com.example.photography.model.enums.UserRole;
import com.example.photography.repository.UserRepository;
import com.example.photography.util.JwtUtil;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private static final String TOKEN = "test-token";
    private static final String USERNAME = "manager";

    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final FilterChain filterChain = mock(FilterChain.class);
    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthenticationFilter(jwtUtil, userRepository);
        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + TOKEN);
        response = new MockHttpServletResponse();
        when(jwtUtil.getUsernameFromToken(TOKEN)).thenReturn(USERNAME);
        when(jwtUtil.validateToken(TOKEN, USERNAME)).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void directorSuppressesLowerLevelAdvisorAuthority() throws Exception {
        User user = userWithRoles(3, UserRole.DIRECTOR, UserRole.ADVISOR);
        authenticate(user, 3);

        assertEquals(Set.of("ROLE_DIRECTOR", "ROLE_ADMIN"), authorities());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void ministerAndAdvisorAtTheSameLevelBothBecomeAuthorities() throws Exception {
        User user = userWithRoles(4, UserRole.MINISTER, UserRole.ADVISOR);
        authenticate(user, 4);

        assertEquals(Set.of("ROLE_MINISTER", "ROLE_ADVISOR", "ROLE_ADMIN"), authorities());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void tokenVersionMismatchDoesNotAuthenticate() throws Exception {
        User user = userWithRoles(6, UserRole.DIRECTOR);
        authenticate(user, 5);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtUtil, never()).validateToken(TOKEN, USERNAME);
        verify(filterChain).doFilter(request, response);
    }

    private void authenticate(User user, int tokenVersion) throws Exception {
        when(userRepository.findByUsernameAndDeletedFalse(USERNAME)).thenReturn(Optional.of(user));
        when(jwtUtil.getTokenVersionFromToken(TOKEN)).thenReturn(tokenVersion);

        filter.doFilter(request, response, filterChain);
    }

    private User userWithRoles(int tokenVersion, UserRole... roles) {
        User user = new User(USERNAME, "password-123", "Manager", UserRole.MEMBER);
        user.setId(42L);
        user.setRoles(List.of(roles));
        user.setTokenVersion(tokenVersion);
        return user;
    }

    private Set<String> authorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals(USERNAME, authentication.getName());
        assertEquals(42L, authentication.getDetails());
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());
    }
}
