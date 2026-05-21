package com.restaurant.booking.service;

import com.restaurant.booking.model.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.enabled:false}") private boolean enabled;
    @Value("${spring.mail.username:}")     private String  fromAddress;
    @Value("${app.base-url:http://localhost:8080}") private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean isEnabled() { return enabled; }

    // ── Booking confirmation ──────────────────────────────────────────────────

    public void sendBookingConfirmation(Booking booking) {
        if (!enabled || booking.getUser() == null) return;
        String subject = "Booking Confirmed — CafeBook (" + bookingRef(booking.getId()) + ")";
        String body = "<h2 style='font-family:serif'>Your reservation is confirmed ☕</h2>"
            + "<p>Hi " + booking.getUser().getName() + ",</p>"
            + "<p>Your table has been booked. Here are your details:</p>"
            + detailsTable(booking)
            + "<p>Please show your booking reference <strong>" + bookingRef(booking.getId())
            + "</strong> when you arrive.</p>"
            + footer();
        send(booking.getUser().getEmail(), subject, body);
    }

    // ── Status change notification ────────────────────────────────────────────

    public void sendStatusUpdate(Booking booking) {
        if (!enabled || booking.getUser() == null) return;
        String status = booking.getStatus().name();
        String subject = "Booking " + status + " — CafeBook (" + bookingRef(booking.getId()) + ")";
        String body = "<h2 style='font-family:serif'>Booking update</h2>"
            + "<p>Hi " + booking.getUser().getName() + ",</p>"
            + "<p>Your booking status has been updated to <strong>" + status + "</strong>.</p>"
            + detailsTable(booking)
            + footer();
        send(booking.getUser().getEmail(), subject, body);
    }

    // ── Password reset ────────────────────────────────────────────────────────

    public void sendPasswordReset(String toEmail, String token) {
        if (!enabled) return;
        String link    = baseUrl + "/reset-password.html?token=" + token;
        String subject = "Reset Your CafeBook Password";
        String body = "<h2 style='font-family:serif'>Password Reset Request</h2>"
            + "<p>Click the button below to set a new password. This link expires in 1 hour.</p>"
            + "<p style='margin:20px 0'>"
            + "<a href='" + link + "' style='background:#2c1a0e;color:#fff;padding:10px 22px;"
            + "border-radius:4px;text-decoration:none;font-weight:bold'>Reset Password</a></p>"
            + "<p style='color:#999;font-size:12px'>If you did not request this, ignore this email.</p>"
            + footer();
        send(toEmail, subject, body);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void send(String to, String subject, String html) {
        try {
            var msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            // Email failure must never crash the main request
            System.err.println("[EmailService] Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    private String detailsTable(Booking b) {
        String seat = b.getTable() != null ? "Seat " + b.getTable().getTableNumber() : "—";
        String zone = (b.getTable() != null && b.getTable().getZone() != null)
                      ? b.getTable().getZone().getName() : "";
        return "<table style='border-collapse:collapse;width:100%;max-width:400px;margin:16px 0'>"
            + row("Reference",  bookingRef(b.getId()))
            + row("Seat",       seat + (zone.isEmpty() ? "" : " — " + zone))
            + row("Date",       b.getBookingDate())
            + row("Time",       b.getBookingTime())
            + row("Guests",     String.valueOf(b.getPartySize()))
            + row("Status",     b.getStatus().name())
            + "</table>";
    }

    private String row(String label, String value) {
        return "<tr><td style='padding:6px 10px;background:#f5f0eb;font-weight:bold;border:1px solid #ddd'>"
            + label + "</td><td style='padding:6px 10px;border:1px solid #ddd'>" + value + "</td></tr>";
    }

    private String footer() {
        return "<hr style='margin-top:24px;border:none;border-top:1px solid #eee'>"
            + "<p style='color:#999;font-size:11px'>☕ CafeBook — Reserve Your Perfect Table</p>";
    }

    // ── Email verification ────────────────────────────────────────────────────

    public void sendEmailVerification(String toEmail, String token) {
        if (!enabled) return;
        String link    = baseUrl + "/verify-email.html?token=" + token;
        String subject = "Verify Your CafeBook Email Address";
        String body = "<h2 style='font-family:serif'>One last step ☕</h2>"
            + "<p>Thanks for joining CafeBook! Please verify your email address to start making reservations.</p>"
            + "<p style='margin:20px 0'>"
            + "<a href='" + link + "' style='background:#2c1a0e;color:#fff;padding:10px 22px;"
            + "border-radius:4px;text-decoration:none;font-weight:bold'>Verify My Email</a></p>"
            + "<p style='color:#999;font-size:12px'>If you did not create a CafeBook account, ignore this email.</p>"
            + footer();
        send(toEmail, subject, body);
    }

    private String bookingRef(Long id) {
        return "CB-" + String.format("%05d", id);
    }
}
