package com.web.web.Controller;

import com.web.web.Dto.BookingDTO;
import com.web.web.Entity.Booking;
import com.web.web.Service.BookingService;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // ══════════════════════════════════════════
    // USER ENDPOINTS
    // ══════════════════════════════════════════

    /** Người dùng đặt bàn → tự động CONFIRMED */
    @PostMapping("/create")
    public ResponseEntity<?> createBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BookingDTO dto) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        try {
            Booking booking = bookingService.createBooking(dto, userDetails.getUsername());
            BookingDTO result = bookingService.getBookingById(
                    booking.getId(), userDetails.getUsername(), false);
            return ResponseEntity.status(201).body(Map.of(
                    "message", "Đặt bàn thành công",
                    "data", result));
        } catch (RuntimeException e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống (NullPointerException)";
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }

    /** Người dùng xem lịch sử đặt bàn */
    @GetMapping("/history")
    public ResponseEntity<?> getUserBookings(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        List<BookingDTO> bookings = bookingService.getUserBookings(userDetails.getUsername());
        return ResponseEntity.ok(bookings);
    }

    /** Người dùng xem chi tiết đơn đặt bàn */
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getBookingById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        try {
            boolean isStaff = hasStaffRole(userDetails);
            BookingDTO bookingDTO = bookingService.getBookingById(
                    id, userDetails.getUsername(), isStaff);
            return ResponseEntity.ok(bookingDTO);
        } catch (RuntimeException e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống (NullPointerException)";
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }

    /** Người dùng tự hủy đơn đặt bàn */
    @PutMapping("/user/cancel/{id}")
    public ResponseEntity<?> cancelBookingByUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        try {
            BookingDTO result = bookingService.cancelBookingByUser(id, userDetails.getUsername());
            return ResponseEntity.ok(Map.of(
                    "message", "Đã hủy đơn đặt bàn thành công",
                    "data", result));
        } catch (RuntimeException e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống (NullPointerException)";
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }

    // ══════════════════════════════════════════
    // ADMIN / STAFF / BOSS ENDPOINTS
    // ══════════════════════════════════════════

    /** Xem tất cả đơn đặt bàn */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'BOSS')")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    /** Xem đặt bàn CONFIRMED hôm nay (cho TableManager panel) */
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'BOSS')")
    public ResponseEntity<List<BookingDTO>> getTodayBookings() {
        LocalDate today = LocalDate.now();
        List<Booking> bookings = bookingService.getConfirmedBookingsForDate(today);
        // Reuse service toDTO via getBookingById is not efficient;
        // Instead, get all bookings and filter
        List<BookingDTO> allBookings = bookingService.getAllBookings();
        List<BookingDTO> todayConfirmed = allBookings.stream()
                .filter(b -> b.getBookingDate() != null && b.getBookingDate().equals(today)
                        && "CONFIRMED".equals(b.getStatus()))
                .toList();
        return ResponseEntity.ok(todayConfirmed);
    }

    /** Kiểm tra bàn trống cho ngày + giờ cụ thể */
    @GetMapping("/check-availability")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> checkTableAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        try {
            Map<Long, Boolean> availability = bookingService.checkTableAvailability(date, time);
            return ResponseEntity.ok(availability);
        } catch (RuntimeException e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống (NullPointerException)";
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }

    /** Nhân viên đánh dấu khách đã đến (check-in) */
    @PutMapping("/check-in/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'BOSS')")
    public ResponseEntity<?> checkInBooking(@PathVariable Long id) {
        try {
            BookingDTO result = bookingService.checkInBooking(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Đã đánh dấu khách đến thành công",
                    "data", result));
        } catch (RuntimeException e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống (NullPointerException)";
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }

    /** Nhân viên đánh dấu hoàn thành (đã thanh toán) */
    @PutMapping("/complete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'BOSS')")
    public ResponseEntity<?> completeBooking(@PathVariable Long id) {
        try {
            BookingDTO result = bookingService.completeBooking(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Đã hoàn thành đơn đặt bàn",
                    "data", result));
        } catch (RuntimeException e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống (NullPointerException)";
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }

    /** Nhân viên hủy đơn (bắt buộc có lý do) */
    @PutMapping("/staff/cancel/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'BOSS')")
    public ResponseEntity<?> cancelBookingByStaff(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String cancelReason = body.get("cancelReason");
            BookingDTO result = bookingService.cancelBookingByStaff(id, cancelReason);
            return ResponseEntity.ok(Map.of(
                    "message", "Đã hủy đơn đặt bàn",
                    "data", result));
        } catch (RuntimeException e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống (NullPointerException)";
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }

    /** Nhân viên đánh dấu no-show (khách không đến) */
    @PutMapping("/no-show/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'BOSS')")
    public ResponseEntity<?> noShowBooking(@PathVariable Long id) {
        try {
            BookingDTO result = bookingService.noShowBooking(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Đã đánh dấu khách không đến",
                    "data", result));
        } catch (RuntimeException e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống (NullPointerException)";
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }

    /** Xóa đơn đặt bàn khỏi hệ thống */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        try {
            bookingService.deleteBooking(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa đơn đặt bàn"));
        } catch (RuntimeException e) {
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống (NullPointerException)";
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }

    // ══════════════════════════════════════════
    // HELPER
    // ══════════════════════════════════════════

    /** Kiểm tra user có role ADMIN/STAFF/BOSS không */
    private boolean hasStaffRole(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN") ||
                        auth.equals("ROLE_STAFF") ||
                        auth.equals("ROLE_BOSS"));
    }
}