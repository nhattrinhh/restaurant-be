package com.web.web.Repository;

import com.web.web.Entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
        List<Booking> findByUserUsername(String username);

        List<Booking> findByBookingDateAndStatusOrderByBookingTimeAsc(LocalDate date, Booking.BookingStatus status);
}
