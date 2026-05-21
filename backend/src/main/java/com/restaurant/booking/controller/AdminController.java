package com.restaurant.booking.controller;

import com.restaurant.booking.model.Booking;
import com.restaurant.booking.model.RestaurantTable;
import com.restaurant.booking.repository.TableRepository;
import com.restaurant.booking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only endpoints (requires ADMIN role).
 * Bookings : view all, change status
 * Tables   : add, toggle availability, delete
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final BookingService bookingService;
    private final TableRepository tableRepo;

    public AdminController(BookingService bookingService, TableRepository tableRepo) {
        this.bookingService = bookingService;
        this.tableRepo = tableRepo;
    }

    // ── Bookings ──────────────────────────────────────────────────────────────

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PatchMapping("/bookings/{id}/status")
    public ResponseEntity<Booking> updateStatus(@PathVariable Long id,
                                                 @RequestParam Booking.BookingStatus status) {
        return ResponseEntity.ok(bookingService.updateStatus(id, status));
    }

    // ── Tables ────────────────────────────────────────────────────────────────

    @PostMapping("/tables")
    public ResponseEntity<RestaurantTable> addTable(@RequestBody RestaurantTable table) {
        return ResponseEntity.ok(tableRepo.save(table));
    }

    @PatchMapping("/tables/{id}/availability")
    public ResponseEntity<RestaurantTable> toggleTable(@PathVariable Long id,
                                                        @RequestParam boolean available) {
        RestaurantTable t = tableRepo.findById(id).orElseThrow();
        t.setIsAvailable(available);
        return ResponseEntity.ok(tableRepo.save(t));
    }

    @DeleteMapping("/tables/{id}")
    public ResponseEntity<Void> deleteTable(@PathVariable Long id) {
        tableRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
