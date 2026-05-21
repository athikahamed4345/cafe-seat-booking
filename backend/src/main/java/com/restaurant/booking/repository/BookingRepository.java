package com.restaurant.booking.repository;

import com.restaurant.booking.model.Booking;
import com.restaurant.booking.model.RestaurantTable;
import com.restaurant.booking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserOrderByCreatedAtDesc(User user);

    List<Booking> findAllByOrderByCreatedAtDesc();

    boolean existsByUserAndBookingDateAndBookingTimeAndStatusNotIn(
        User user, String bookingDate, String bookingTime,
        java.util.Collection<Booking.BookingStatus> excludedStatuses);

    boolean existsByTableAndBookingDateAndBookingTimeAndStatusNotIn(
        RestaurantTable table, String bookingDate, String bookingTime,
        java.util.Collection<Booking.BookingStatus> excludedStatuses);
}
