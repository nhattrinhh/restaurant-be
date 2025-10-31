package com.web.web.Service;

import com.web.web.Dto.BookingDTO;
import com.web.web.Entity.Booking;
import com.web.web.Entity.User;
import com.web.web.Repository.BookingRepository;
import com.web.web.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    // Người dùng đặt bàn
    @Transactional
    public Booking createBooking(BookingDTO dto, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        Booking booking = new Booking();
        booking.setFullName(dto.getFullName());
        booking.setPhoneNumber(dto.getPhoneNumber());
        booking.setBookingDate(dto.getBookingDate());
        booking.setBookingTime(dto.getBookingTime());
        booking.setNumberOfGuests(dto.getNumberOfGuests());
        booking.setArea(dto.getArea());
        booking.setSpecialRequests(dto.getSpecialRequests());
        booking.setCreatedAt(LocalDateTime.now());
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setUser(user);

        return bookingRepository.save(booking);
    }

    // Người dùng xem lịch sử đặt bàn
    public List<BookingDTO> getUserBookings(String username) {
        return bookingRepository.findByUserUsername(username).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Admin xem tất cả đơn đặt bàn
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Admin và người dùng xem chi tiết đơn đặt bàn
    public BookingDTO getBookingById(Long id, String username, String role) {
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isEmpty()) {
            throw new RuntimeException("Đơn đặt bàn không tồn tại");
        }
        Booking b = booking.get();
        // Kiểm tra quyền: Nếu không phải ADMIN, người dùng chỉ xem được đơn của chính họ
        if (!role.equals("ROLE_ADMIN") && !b.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền xem chi tiết đơn đặt bàn này");
        }
        return toDTO(b);
    }

    // Admin xác nhận đơn đặt bàn
    @Transactional
    public BookingDTO confirmBooking(Long id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isEmpty()) {
            throw new RuntimeException("Đơn đặt bàn không tồn tại");
        }
        Booking b = booking.get();
        b.setStatus(Booking.BookingStatus.CONFIRMED);
        return toDTO(bookingRepository.save(b));
    }

    // Admin hủy đơn đặt bàn
    @Transactional
    public BookingDTO cancelBooking(Long id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isEmpty()) {
            throw new RuntimeException("Đơn đặt bàn không tồn tại");
        }
        Booking b = booking.get();
        b.setStatus(Booking.BookingStatus.CANCELLED);
        return toDTO(bookingRepository.save(b));
    }
    // Người dùng yêu cầu hủy đơn đặt bàn
    @Transactional
    public BookingDTO cancelBookingByUser(Long id, String username) {
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isEmpty()) {
            throw new RuntimeException("Đơn đặt bàn không tồn tại");
        }
        Booking b = booking.get();
        if (!b.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn đặt bàn này");
        }
        if (b.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy đơn đặt bàn ở trạng thái chờ xác nhận");
        }
        b.setStatus(Booking.BookingStatus.CANCEL_REQUESTED);
        return toDTO(bookingRepository.save(b));
    }

    // Admin đồng ý hủy đơn đặt bàn
    @Transactional
    public BookingDTO approveCancelBooking(Long id, String role) {
        if (!role.equals("ROLE_ADMIN")) {
            throw new RuntimeException("Chỉ admin mới có quyền đồng ý hủy đơn đặt bàn");
        }
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isEmpty()) {
            throw new RuntimeException("Đơn đặt bàn không tồn tại");
        }
        Booking b = booking.get();
        if (b.getStatus() != Booking.BookingStatus.CANCEL_REQUESTED) {
            throw new RuntimeException("Đơn đặt bàn không ở trạng thái yêu cầu hủy");
        }
        b.setStatus(Booking.BookingStatus.CANCELLED);
        return toDTO(bookingRepository.save(b));
    }

    // Admin từ chối hủy đơn đặt bàn
    @Transactional
    public BookingDTO rejectCancelBooking(Long id, String role) {
        if (!role.equals("ROLE_ADMIN")) {
            throw new RuntimeException("Chỉ admin mới có quyền từ chối hủy đơn đặt bàn");
        }
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isEmpty()) {
            throw new RuntimeException("Đơn đặt bàn không tồn tại");
        }
        Booking b = booking.get();
        if (b.getStatus() != Booking.BookingStatus.CANCEL_REQUESTED) {
            throw new RuntimeException("Đơn đặt bàn không ở trạng thái yêu cầu hủy");
        }
        b.setStatus(Booking.BookingStatus.CONFIRMED);
        return toDTO(bookingRepository.save(b));
    }

    // Admin xóa đơn đặt bàn
    @Transactional
    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new RuntimeException("Đơn đặt bàn không tồn tại");
        }
        bookingRepository.deleteById(id);
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
