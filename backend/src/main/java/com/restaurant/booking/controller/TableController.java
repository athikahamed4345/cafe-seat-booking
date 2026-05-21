package com.restaurant.booking.controller;

import com.restaurant.booking.model.Booking;
import com.restaurant.booking.model.RestaurantTable;
import com.restaurant.booking.repository.TableRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public table endpoints — no login needed.
 * GET /api/tables           — all tables (used to load zone dropdown)
 * GET /api/tables/available — tables free for a date/time/party size
 */
@RestController
@RequestMapping("/api/tables")
public class TableController {

    private final TableRepository tableRepo;

    public TableController(TableRepository tableRepo) {
        this.tableRepo = tableRepo;
    }

    @GetMapping
    public ResponseEntity<List<RestaurantTable>> all() {
        return ResponseEntity.ok(tableRepo.findAll());
    }

    @GetMapping("/available")
    public ResponseEntity<List<RestaurantTable>> available(
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam int partySize) {
        return ResponseEntity.ok(tableRepo.findAvailable(date, time, partySize,
            List.of(Booking.BookingStatus.CANCELLED, Booking.BookingStatus.NO_SHOW)));
    }
}
