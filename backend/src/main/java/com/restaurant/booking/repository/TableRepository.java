package com.restaurant.booking.repository;

import com.restaurant.booking.model.Booking;
import com.restaurant.booking.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TableRepository extends JpaRepository<RestaurantTable, Long> {

    @Query("SELECT t FROM RestaurantTable t WHERE t.isAvailable = true " +
           "AND t.maxCapacity >= :partySize " +
           "AND t.id NOT IN (" +
           "  SELECT b.table.id FROM Booking b " +
           "  WHERE b.bookingDate = :date AND b.bookingTime = :time " +
           "  AND b.status NOT IN :excluded)")
    List<RestaurantTable> findAvailable(@Param("date") String date,
                                        @Param("time") String time,
                                        @Param("partySize") int partySize,
                                        @Param("excluded") Collection<Booking.BookingStatus> excluded);
}
