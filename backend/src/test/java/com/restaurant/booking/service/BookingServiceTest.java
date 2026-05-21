package com.restaurant.booking.service;

import com.restaurant.booking.dto.BookingRequest;
import com.restaurant.booking.model.Booking;
import com.restaurant.booking.model.RestaurantTable;
import com.restaurant.booking.model.User;
import com.restaurant.booking.repository.BookingRepository;
import com.restaurant.booking.repository.TableRepository;
import com.restaurant.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock BookingRepository bookingRepo;
    @Mock TableRepository   tableRepo;
    @Mock UserRepository    userRepo;
    @Mock EmailService      emailService;

    @InjectMocks BookingService bookingService;

    private User           user;
    private RestaurantTable table;

    @BeforeEach
    void setUp() {
        user  = new User();
        user.setId(1L);
        user.setEmail("test@cafe.com");
        user.setName("Test User");

        table = new RestaurantTable();
        table.setId(1L);
        table.setTableNumber("W1");
        table.setIsAvailable(true);
        table.setMaxCapacity(4);
        table.setMinCapacity(1);
    }

    // ── create() ─────────────────────────────────────────────────────────────

    @Test
    void create_savesBookingAndSendsEmail() {
        when(userRepo.findByEmail("test@cafe.com")).thenReturn(Optional.of(user));
        when(tableRepo.findById(1L)).thenReturn(Optional.of(table));
        when(bookingRepo.existsByUserAndBookingDateAndBookingTimeAndStatusNotIn(
            any(), any(), any(), any())).thenReturn(false);

        Booking saved = new Booking();
        saved.setId(1L);
        saved.setUser(user);
        saved.setTable(table);
        when(bookingRepo.save(any())).thenReturn(saved);

        BookingRequest req = new BookingRequest();
        req.setTableId(1L);
        req.setBookingDate(LocalDate.now().plusDays(1).toString());
        req.setBookingTime("10:00");
        req.setPartySize(2);

        Booking result = bookingService.create("test@cafe.com", req);

        assertNotNull(result);
        verify(bookingRepo).save(any(Booking.class));
        verify(emailService).sendBookingConfirmation(saved);
    }

    @Test
    void create_rejectsPastDate() {
        BookingRequest req = new BookingRequest();
        req.setTableId(1L);
        req.setBookingDate("2020-01-01");
        req.setBookingTime("10:00");
        req.setPartySize(2);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> bookingService.create("test@cafe.com", req));
        assertEquals("Booking date cannot be in the past.", ex.getMessage());
    }

    @Test
    void create_rejectsZeroPartySize() {
        BookingRequest req = new BookingRequest();
        req.setTableId(1L);
        req.setBookingDate(LocalDate.now().plusDays(1).toString());
        req.setBookingTime("10:00");
        req.setPartySize(0);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> bookingService.create("test@cafe.com", req));
        assertEquals("Party size must be at least 1.", ex.getMessage());
    }

    @Test
    void create_rejectsDuplicateBooking() {
        when(userRepo.findByEmail("test@cafe.com")).thenReturn(Optional.of(user));
        when(tableRepo.findById(1L)).thenReturn(Optional.of(table));
        when(bookingRepo.existsByUserAndBookingDateAndBookingTimeAndStatusNotIn(
            any(), any(), any(), any())).thenReturn(true);

        BookingRequest req = new BookingRequest();
        req.setTableId(1L);
        req.setBookingDate(LocalDate.now().plusDays(1).toString());
        req.setBookingTime("10:00");
        req.setPartySize(2);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> bookingService.create("test@cafe.com", req));
        assertEquals("You already have a booking at this date and time.", ex.getMessage());
    }

    // ── cancel() ─────────────────────────────────────────────────────────────

    @Test
    void cancel_setsStatusCancelledForOwner() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepo.save(any())).thenReturn(booking);

        Booking result = bookingService.cancel(1L, "test@cafe.com");

        assertEquals(Booking.BookingStatus.CANCELLED, result.getStatus());
        verify(emailService).sendStatusUpdate(booking);
    }

    @Test
    void cancel_throwsForWrongUser() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(booking));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> bookingService.cancel(1L, "other@cafe.com"));
        assertEquals("Unauthorized.", ex.getMessage());
    }
}
