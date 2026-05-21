package com.restaurant.booking.controller;

import com.restaurant.booking.dto.BookingRequest;
import com.restaurant.booking.model.Booking;
import com.restaurant.booking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Booking> create(@RequestBody BookingRequest req, Authentication auth) {
        return ResponseEntity.ok(bookingService.create(auth.getName(), req));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Booking>> myBookings(Authentication auth) {
        return ResponseEntity.ok(bookingService.getMyBookings(auth.getName()));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancel(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(bookingService.cancel(id, auth.getName()));
    }
}
