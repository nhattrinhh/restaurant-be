package com.web.web.Repository;

import com.web.web.Entity.Booking;
import com.web.web.Entity.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Lịch sử đặt bàn của user
    List<Booking> findByUserUsername(String username);

    // Booking theo ngày + status (dùng cho admin/table manager)
    List<Booking> findByBookingDateAndStatusOrderByBookingTimeAsc(
            LocalDate date, BookingStatus status);

    /**
     * Tìm booking active (CONFIRMED/CHECKED_IN) cho 1 bàn vào ngày cụ thể.
     * Logic check trùng sẽ được xử lý trong Java (BookingService).
     */
    List<Booking> findByTableIdAndBookingDateAndStatusIn(
            Long tableId, LocalDate bookingDate, List<BookingStatus> statuses);

    /**
     * Tất cả booking active cho một ngày (dùng cho check table availability).
     */
    List<Booking> findByBookingDateAndStatusInOrderByBookingTimeAsc(
            LocalDate date, List<BookingStatus> statuses);
}
