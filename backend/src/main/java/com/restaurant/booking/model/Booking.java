package com.restaurant.booking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    public enum BookingStatus { PENDING, CONFIRMED, COMPLETED, NO_SHOW, CANCELLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "table_id")
    private RestaurantTable table;
    private String bookingDate;
    private String bookingTime;
    private int partySize;
    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;
    private String specialOccasion;
    private String specialNotes;
    private double depositAmount;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User u) { this.user = u; }
    public RestaurantTable getTable() { return table; }
    public void setTable(RestaurantTable t) { this.table = t; }
    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String v) { this.bookingDate = v; }
    public String getBookingTime() { return bookingTime; }
    public void setBookingTime(String v) { this.bookingTime = v; }
    public int getPartySize() { return partySize; }
    public void setPartySize(int v) { this.partySize = v; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus v) { this.status = v; }
    public String getSpecialOccasion() { return specialOccasion; }
    public void setSpecialOccasion(String v) { this.specialOccasion = v; }
    public String getSpecialNotes() { return specialNotes; }
    public void setSpecialNotes(String v) { this.specialNotes = v; }
    public double getDepositAmount() { return depositAmount; }
    public void setDepositAmount(double v) { this.depositAmount = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
