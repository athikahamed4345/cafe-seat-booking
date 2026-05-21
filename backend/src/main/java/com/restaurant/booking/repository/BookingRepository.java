package com.restaurant.booking.repository;

import com.restaurant.booking.model.Booking;
import com.restaurant.booking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserOrderByCreatedAtDesc(User user);

    boolean existsByUserAndBookingDateAndBookingTimeAndStatusNotIn(
        User user, String bookingDate, String bookingTime,
        java.util.Collection<Booking.BookingStatus> excludedStatuses);
}
