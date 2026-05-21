package com.restaurant.booking.controller;

import com.restaurant.booking.model.Booking;
import com.restaurant.booking.model.User;
import com.restaurant.booking.repository.BookingRepository;
import com.restaurant.booking.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository    userRepo;
    private final BookingRepository bookingRepo;
    private final PasswordEncoder   passwordEncoder;

    public UserController(UserRepository userRepo, BookingRepository bookingRepo,
                          PasswordEncoder passwordEncoder) {
        this.userRepo        = userRepo;
        this.bookingRepo     = bookingRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // PATCH /api/user/password — change own password
    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body,
                                            Authentication auth) {
        String currentPassword = body.get("currentPassword");
        String newPassword     = body.get("newPassword");

        if (newPassword == null || newPassword.length() < 6)
            return ResponseEntity.badRequest()
                .body(Map.of("error", "New password must be at least 6 characters."));

        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        if (!passwordEncoder.matches(currentPassword, user.getPassword()))
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Current password is incorrect."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
    }

    // DELETE /api/user/me — GDPR account deletion
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow();

        // Anonymise bookings — keep records for cafe stats but remove personal data
        bookingRepo.findByUserOrderByCreatedAtDesc(user).forEach(b -> {
            b.setStatus(Booking.BookingStatus.CANCELLED);
            b.setUser(null);
            b.setSpecialNotes(null);
            b.setSpecialOccasion(null);
            bookingRepo.save(b);
        });

        userRepo.delete(user);
        return ResponseEntity.noContent().build();
    }
}
