package com.restaurant.booking.service;

import com.restaurant.booking.config.JwtFilter;
import com.restaurant.booking.model.User;
import com.restaurant.booking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository  userRepo;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtFilter       jwtFilter;
    @Mock EmailService    emailService;

    @InjectMocks AuthService authService;

    // ── register() ────────────────────────────────────────────────────────────

    @Test
    void register_savesUserAndSendsVerificationEmail() {
        when(userRepo.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        authService.register("Jane", "jane@cafe.com", "", "pass123", "", false, true);

        verify(userRepo).save(any(User.class));
        verify(emailService).sendEmailVerification(eq("jane@cafe.com"), anyString());
    }

    @Test
    void register_rejectsBlankName() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> authService.register("", "jane@cafe.com", "", "pass123", "", false, true));
        assertEquals("Full name is required.", ex.getMessage());
    }

    @Test
    void register_rejectsInvalidEmail() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> authService.register("Jane", "notanemail", "", "pass123", "", false, true));
        assertEquals("Please enter a valid email address.", ex.getMessage());
    }

    @Test
    void register_rejectsShortPassword() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> authService.register("Jane", "jane@cafe.com", "", "abc", "", false, true));
        assertEquals("Password must be at least 6 characters.", ex.getMessage());
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepo.findByEmail(any())).thenReturn(Optional.of(new User()));
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> authService.register("Jane", "jane@cafe.com", "", "pass123", "", false, true));
        assertEquals("Email already registered.", ex.getMessage());
    }

    @Test
    void register_rejectsWithoutGdprConsent() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> authService.register("Jane", "jane@cafe.com", "", "pass123", "", false, false));
        assertEquals("You must accept the Privacy Policy to register.", ex.getMessage());
    }

    // ── login() ───────────────────────────────────────────────────────────────

    @Test
    void login_returnsTokenForVerifiedUser() {
        User user = new User();
        user.setEmail("jane@cafe.com");
        user.setPassword("hashed");
        user.setEmailVerified(true);

        when(userRepo.findByEmail("jane@cafe.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass123", "hashed")).thenReturn(true);
        when(jwtFilter.createToken("jane@cafe.com")).thenReturn("jwt-token");

        Map<String, Object> result = authService.login("jane@cafe.com", "pass123");

        assertEquals("jwt-token", result.get("token"));
    }

    @Test
    void login_rejectsUnverifiedEmail() {
        User user = new User();
        user.setEmail("jane@cafe.com");
        user.setPassword("hashed");
        user.setEmailVerified(false);

        when(userRepo.findByEmail("jane@cafe.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> authService.login("jane@cafe.com", "pass123"));
        assertTrue(ex.getMessage().contains("verify your email"));
    }

    @Test
    void login_rejectsWrongPassword() {
        User user = new User();
        user.setEmail("jane@cafe.com");
        user.setPassword("hashed");

        when(userRepo.findByEmail("jane@cafe.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> authService.login("jane@cafe.com", "wrong"));
        assertEquals("Invalid email or password.", ex.getMessage());
    }

    // ── resetPassword() ───────────────────────────────────────────────────────

    @Test
    void resetPassword_updatesPasswordAndClearsToken() {
        User user = new User();
        user.setResetToken("valid-token");
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));

        when(userRepo.findByResetToken("valid-token")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("newHashed");
        when(userRepo.save(any())).thenReturn(user);

        authService.resetPassword("valid-token", "newpass");

        assertNull(user.getResetToken());
        assertNull(user.getResetTokenExpiry());
        verify(userRepo).save(user);
    }

    @Test
    void resetPassword_rejectsExpiredToken() {
        User user = new User();
        user.setResetToken("old-token");
        user.setResetTokenExpiry(LocalDateTime.now().minusHours(2));

        when(userRepo.findByResetToken("old-token")).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> authService.resetPassword("old-token", "newpass"));
        assertTrue(ex.getMessage().contains("expired"));
    }
}
