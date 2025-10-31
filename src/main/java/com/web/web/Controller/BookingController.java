package com.web.web.Controller;

import com.web.web.Dto.BookingDTO;
import com.web.web.Entity.Booking;
import com.web.web.Service.BookingService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Người dùng đặt bàn
    @PostMapping("/create")
    public ResponseEntity<BookingDTO> createBooking(@AuthenticationPrincipal UserDetails userDetails,
                                                    @Valid @RequestBody BookingDTO dto) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        Booking booking = bookingService.createBooking(dto, userDetails.getUsername());
        return ResponseEntity.ok(toDTO(booking));
    }

    // Người dùng xem lịch sử đặt bàn
    @GetMapping("/history")
    public ResponseEntity<List<BookingDTO>> getUserBookings(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        List<BookingDTO> bookings = bookingService.getUserBookings(userDetails.getUsername());
        return ResponseEntity.ok(bookings);
    }

    // Admin xem tất cả đơn đặt bàn
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // Admin và người dùng xem chi tiết đơn đặt bàn
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.equals("ROLE_ADMIN") || auth.equals("ROLE_USER"))
                    .findFirst()
                    .orElse("ROLE_USER");
            BookingDTO bookingDTO = bookingService.getBookingById(id, userDetails.getUsername(), role);
            return ResponseEntity.ok(bookingDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new BookingDTO(null, null, null, null, null, 0, null, null, null, null, e.getMessage()));
        }
    }

    // Admin xác nhận đơn đặt bàn
    @PutMapping("/confirm/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingDTO> confirmBooking(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bookingService.confirmBooking(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Admin hủy đơn đặt bàn
    @PutMapping("/cancel/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bookingService.cancelBooking(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Người dùng yêu cầu hủy đơn đặt bàn
    @PutMapping("/user/cancel/{id}")
    public ResponseEntity<BookingDTO> cancelBookingByUser(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            BookingDTO bookingDTO = bookingService.cancelBookingByUser(id, userDetails.getUsername());
            return ResponseEntity.ok(bookingDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new BookingDTO(null, null, null, null, null, 0, null, null, null, null, e.getMessage()));
        }
    }

    // Admin đồng ý hủy đơn đặt bàn
    @PutMapping("/approve-cancel/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingDTO> approveCancelBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.equals("ROLE_ADMIN"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Chỉ admin mới có quyền đồng ý hủy đơn đặt bàn"));
            BookingDTO bookingDTO = bookingService.approveCancelBooking(id, role);
            return ResponseEntity.ok(bookingDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new BookingDTO(null, null, null, null, null, 0, null, null, null, null, e.getMessage()));
        }
    }

    // Admin từ chối hủy đơn đặt bàn
    @PutMapping("/reject-cancel/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingDTO> rejectCancelBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.equals("ROLE_ADMIN"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Chỉ admin mới có quyền từ chối hủy đơn đặt bàn"));
            BookingDTO bookingDTO = bookingService.rejectCancelBooking(id, role);
            return ResponseEntity.ok(bookingDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new BookingDTO(null, null, null, null, null, 0, null, null, null, null, e.getMessage()));
        }
    }

    // Admin xóa đơn đặt bàn
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        try {
            bookingService.deleteBooking(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Chuyển từ Entity sang DTO
    private BookingDTO toDTO(Booking booking) {
        return new BookingDTO(
                booking.getId(),
                booking.getFullName(),
                booking.getPhoneNumber(),
                booking.getBookingDate(),
                booking.getBookingTime(),
                booking.getNumberOfGuests(),
                booking.getArea(),
                booking.getSpecialRequests(),
                booking.getCreatedAt(),
                booking.getStatus().name(),
                booking.getUser().getUsername()
        );
    }
}