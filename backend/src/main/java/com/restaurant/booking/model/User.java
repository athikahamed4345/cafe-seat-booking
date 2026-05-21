package com.restaurant.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    public enum Role { ADMIN, GUEST }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    private String phone;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role = Role.GUEST;
    private String dietaryNotes;
    private boolean accessibilityNeeded;
    private boolean gdprConsent    = false;
    private boolean emailVerified  = false;
    private String  resetToken;
    private LocalDateTime resetTokenExpiry;
    private String  verifyToken;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    @JsonIgnore
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getDietaryNotes() { return dietaryNotes; }
    public void setDietaryNotes(String v) { this.dietaryNotes = v; }
    public boolean isAccessibilityNeeded() { return accessibilityNeeded; }
    public void setAccessibilityNeeded(boolean v) { this.accessibilityNeeded = v; }
    public boolean isGdprConsent() { return gdprConsent; }
    public void setGdprConsent(boolean v) { this.gdprConsent = v; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean v) { this.emailVerified = v; }
    @JsonIgnore
    public String getResetToken() { return resetToken; }
    public void setResetToken(String v) { this.resetToken = v; }
    @JsonIgnore
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime v) { this.resetTokenExpiry = v; }
    @JsonIgnore
    public String getVerifyToken() { return verifyToken; }
    public void setVerifyToken(String v) { this.verifyToken = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
