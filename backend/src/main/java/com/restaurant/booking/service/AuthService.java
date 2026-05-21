package com.restaurant.booking.service;

import com.restaurant.booking.config.JwtFilter;
import com.restaurant.booking.model.User;
import com.restaurant.booking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository  userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtFilter       jwtFilter;
    private final EmailService    emailService;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder,
                       JwtFilter jwtFilter, EmailService emailService) {
        this.userRepo        = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtFilter       = jwtFilter;
        this.emailService    = emailService;
    }

    // ── Register ──────────────────────────────────────────────────────────────

    public void register(String name, String email, String phone,
                         String password, String dietaryNotes,
                         boolean accessibilityNeeded, boolean gdprConsent) {
        // Input validation
        if (name == null || name.isBlank())
            throw new RuntimeException("Full name is required.");
        if (email == null || !email.contains("@") || !email.contains("."))
            throw new RuntimeException("Please enter a valid email address.");
        if (password == null || password.length() < 6)
            throw new RuntimeException("Password must be at least 6 characters.");
        if (!gdprConsent)
            throw new RuntimeException("You must accept the Privacy Policy to register.");
        if (userRepo.findByEmail(email.toLowerCase()).isPresent())
            throw new RuntimeException("Email already registered.");

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.toLowerCase().trim());
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setDietaryNotes(dietaryNotes);
        user.setAccessibilityNeeded(accessibilityNeeded);
        user.setGdprConsent(gdprConsent);

        if (emailService.isEnabled()) {
            // Email on: require verification before login
            user.setEmailVerified(false);
            String token = UUID.randomUUID().toString();
            user.setVerifyToken(token);
            userRepo.save(user);
            emailService.sendEmailVerification(email, token);
        } else {
            // Email off: auto-verify so users can log in immediately
            user.setEmailVerified(true);
            userRepo.save(user);
        }
    }

    // ── Verify email ──────────────────────────────────────────────────────────

    public void verifyEmail(String token) {
        User user = userRepo.findByVerifyToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid or expired verification link."));
        user.setEmailVerified(true);
        user.setVerifyToken(null);
        userRepo.save(user);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public Map<String, Object> login(String email, String password) {
        if (email == null || password == null)
            throw new RuntimeException("Email and password are required.");
        User user = userRepo.findByEmail(email.toLowerCase())
            .orElseThrow(() -> new RuntimeException("Invalid email or password."));
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new RuntimeException("Invalid email or password.");
        if (!user.isEmailVerified())
            throw new RuntimeException("Please verify your email address before signing in. Check your inbox.");
        return Map.of("token", jwtFilter.createToken(user.getEmail()), "user", user);
    }

    // ── Forgot password ───────────────────────────────────────────────────────

    public void forgotPassword(String email) {
        userRepo.findByEmail(email.toLowerCase()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepo.save(user);
            emailService.sendPasswordReset(email, token);
        });
    }

    // ── Reset password ────────────────────────────────────────────────────────

    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.length() < 6)
            throw new RuntimeException("Password must be at least 6 characters.");
        User user = userRepo.findByResetToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid or expired reset link."));
        if (user.getResetTokenExpiry() == null ||
                user.getResetTokenExpiry().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Reset link has expired. Please request a new one.");
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepo.save(user);
    }
}
