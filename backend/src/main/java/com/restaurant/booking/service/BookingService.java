package com.restaurant.booking.service;

import com.restaurant.booking.dto.BookingRequest;
import com.restaurant.booking.model.Booking;
import com.restaurant.booking.model.RestaurantTable;
import com.restaurant.booking.model.User;
import com.restaurant.booking.repository.BookingRepository;
import com.restaurant.booking.repository.TableRepository;
import com.restaurant.booking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepo;
    private final TableRepository   tableRepo;
    private final UserRepository    userRepo;
    private final EmailService      emailService;

    public BookingService(BookingRepository bookingRepo, TableRepository tableRepo,
                          UserRepository userRepo, EmailService emailService) {
        this.bookingRepo  = bookingRepo;
        this.tableRepo    = tableRepo;
        this.userRepo     = userRepo;
        this.emailService = emailService;
    }

    public Booking create(String email, BookingRequest req) {
        // ── Input validation ──────────────────────────────────────────────────
        if (req.getTableId() == null)
            throw new RuntimeException("Please select a table.");
        if (req.getBookingDate() == null || req.getBookingDate().isBlank())
            throw new RuntimeException("Please select a date.");
        if (req.getBookingTime() == null || req.getBookingTime().isBlank())
            throw new RuntimeException("Please select a time.");
        if (req.getPartySize() < 1)
            throw new RuntimeException("Party size must be at least 1.");

        // Date must not be in the past
        LocalDate date = LocalDate.parse(req.getBookingDate());
        if (date.isBefore(LocalDate.now()))
            throw new RuntimeException("Booking date cannot be in the past.");

        User user             = userRepo.findByEmail(email).orElseThrow();
        RestaurantTable table = tableRepo.findById(req.getTableId())
            .orElseThrow(() -> new RuntimeException("Selected table not found."));

        if (!table.getIsAvailable())
            throw new RuntimeException("This table is currently unavailable.");

        // ── Duplicate booking check ───────────────────────────────────────────
        boolean duplicate = bookingRepo.existsByUserAndBookingDateAndBookingTimeAndStatusNotIn(
            user,
            req.getBookingDate(),
            req.getBookingTime(),
            List.of(Booking.BookingStatus.CANCELLED, Booking.BookingStatus.NO_SHOW)
        );
        if (duplicate)
            throw new RuntimeException("You already have a booking at this date and time.");

        Booking b = new Booking();
        b.setUser(user);
        b.setTable(table);
        b.setBookingDate(req.getBookingDate());
        b.setBookingTime(req.getBookingTime());
        b.setPartySize(req.getPartySize());
        b.setSpecialOccasion(req.getSpecialOccasion());
        b.setSpecialNotes(req.getSpecialNotes());
        b.setDepositAmount(req.getDepositAmount());
        Booking saved = bookingRepo.save(b);
        emailService.sendBookingConfirmation(saved);
        return saved;
    }

    public List<Booking> getMyBookings(String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        return bookingRepo.findByUserOrderByCreatedAtDesc(user);
    }

    public Booking cancel(Long id, String email) {
        Booking b = bookingRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found."));
        if (!b.getUser().getEmail().equals(email))
            throw new RuntimeException("Unauthorized.");
        b.setStatus(Booking.BookingStatus.CANCELLED);
        Booking saved = bookingRepo.save(b);
        emailService.sendStatusUpdate(saved);
        return saved;
    }

    public List<Booking> getAllBookings() {
        return bookingRepo.findAll();
    }

    public Booking updateStatus(Long id, Booking.BookingStatus status) {
        Booking b = bookingRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found."));
        b.setStatus(status);
        Booking saved = bookingRepo.save(b);
        emailService.sendStatusUpdate(saved);
        return saved;
    }
}
