package com.web.web.Service;

import com.web.web.Dto.BookingDTO;
import com.web.web.Entity.Booking;
import com.web.web.Entity.Booking.BookingStatus;
import com.web.web.Entity.RestaurantTable;
import com.web.web.Entity.User;
import com.web.web.Repository.BookingRepository;
import com.web.web.Repository.RestaurantTableRepository;
import com.web.web.Repository.TableOrderRepository;
import com.web.web.Repository.UserRepository;
import com.web.web.Repository.OrderRepository;
import com.web.web.Repository.PaymentRepository;
import com.web.web.Dto.OrderItemDTO;
import com.web.web.Entity.Order;
import com.web.web.Entity.OrderItem;
import com.web.web.Entity.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class BookingService {

    /** Mỗi lượt đặt bàn kéo dài 2 tiếng */
    private static final int BOOKING_DURATION_HOURS = 2;
    /** Buffer trước giờ hẹn: bàn khóa trước 1h30 */
    private static final int BUFFER_BEFORE_MINUTES = 90;

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RestaurantTableRepository tableRepository;
    private final TableOrderRepository tableOrderRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
            UserRepository userRepository,
            RestaurantTableRepository tableRepository,
            TableOrderRepository tableOrderRepository,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.tableRepository = tableRepository;
        this.tableOrderRepository = tableOrderRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    // ══════════════════════════════════════════
    // TẠO BOOKING
    // ══════════════════════════════════════════

    /**
     * Người dùng đặt bàn — tự động CONFIRMED.
     * Kiểm tra: bàn tồn tại, bàn không bị khóa (OCCUPIED), không trùng thời gian.
     */
    @Transactional
    public Booking createBooking(BookingDTO dto, String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        // Validate bàn tồn tại
        RestaurantTable table = tableRepository.findById(dto.getTableId())
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại"));

        // Kiểm tra bàn có đang sử dụng không (chỉ check ngày hiện tại)
        if (table.getStatus() == RestaurantTable.TableStatus.OCCUPIED) {
            if (dto.getBookingDate().equals(LocalDate.now())) {
                boolean conflictWithOccupied = false;
                try {
                    com.web.web.Entity.TableOrder order = tableOrderRepository
                            .findByTableIdAndStatus(table.getId(), com.web.web.Entity.TableOrder.OrderStatus.OPEN)
                            .orElse(null);
                    if (order != null && order.getEntryTime() != null && !order.getEntryTime().isEmpty()) {
                        LocalTime entryTime = LocalTime.parse(order.getEntryTime());
                        if (isTimeConflict(entryTime, dto.getBookingTime())) {
                            conflictWithOccupied = true;
                        }
                    } else {
                        // Bàn occupied nhưng không có thông tin thời gian -> fallback
                        if (dto.getBookingTime().isBefore(LocalTime.now().plusMinutes(150))) {
                            conflictWithOccupied = true;
                        }
                    }
                } catch (Exception e) {
                    if (dto.getBookingTime().isBefore(LocalTime.now().plusMinutes(150))) {
                        conflictWithOccupied = true;
                    }
                }
                if (conflictWithOccupied) {
                    throw new RuntimeException(
                            "Bàn hiện đang được sử dụng, vui lòng chọn giờ muộn hơn (cách hiện tại 2h30p) hoặc bàn khác.");
                }
            }
        }

        // Kiểm tra trùng thời gian (Java logic) với các đặt bàn trước đó
        List<BookingStatus> activeStatuses = List.of(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN);
        List<Booking> activeBookings = bookingRepository.findByTableIdAndBookingDateAndStatusIn(
                dto.getTableId(), dto.getBookingDate(), activeStatuses);
        for (Booking existing : activeBookings) {
            if (isTimeConflict(existing.getBookingTime(), dto.getBookingTime())) {
                throw new RuntimeException(
                        String.format("Bàn đã được đặt lúc %s. Vui lòng chọn bàn khác hoặc thời gian khác.",
                                existing.getBookingTime().toString()));
            }
        }

        // Tạo booking → CONFIRMED tự động
        Booking booking = new Booking();
        booking.setFullName(dto.getFullName());
        booking.setPhoneNumber(dto.getPhoneNumber());
        booking.setBookingDate(dto.getBookingDate());
        booking.setBookingTime(dto.getBookingTime());
        booking.setNumberOfGuests(dto.getNumberOfGuests());
        booking.setSpecialRequests(dto.getSpecialRequests());
        booking.setCreatedAt(LocalDateTime.now());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUser(user);
        booking.setTable(table);

        return bookingRepository.save(booking);
    }

    // ══════════════════════════════════════════
    // ĐỌC BOOKING
    // ══════════════════════════════════════════

    /** Người dùng xem lịch sử đặt bàn của mình */
    @Transactional(readOnly = true)
    public List<BookingDTO> getUserBookings(String username) {
        return bookingRepository.findByUserUsername(username).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Admin/Staff/Boss xem tất cả đơn đặt bàn */
    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Lấy booking CONFIRMED theo ngày (cho TableManager panel) */
    @Transactional(readOnly = true)
    public List<Booking> getConfirmedBookingsForDate(LocalDate date) {
        return bookingRepository.findByBookingDateAndStatusOrderByBookingTimeAsc(
                date, BookingStatus.CONFIRMED);
    }

    /** Xem chi tiết đơn — admin/staff/boss xem tất cả, user chỉ xem của mình */
    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Long id, String username, boolean isStaff) {
        Booking booking = findBookingOrThrow(id);

        if (!isStaff && !booking.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền xem chi tiết đơn đặt bàn này");
        }
        return toDTO(booking);
    }

    // ══════════════════════════════════════════
    // KIỂM TRA BÀN TRỐNG
    // ══════════════════════════════════════════

    /**
     * Kiểm tra tất cả bàn trống cho một ngày + giờ cụ thể.
     * Trả về map: tableId → isAvailable
     */
    public Map<Long, Boolean> checkTableAvailability(LocalDate date, LocalTime time) {
        List<RestaurantTable> allTables = tableRepository.findAll();
        List<BookingStatus> activeStatuses = List.of(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN);
        List<Booking> activeBookings = bookingRepository.findByBookingDateAndStatusInOrderByBookingTimeAsc(
                date, activeStatuses);

        return allTables.stream().collect(Collectors.toMap(
                RestaurantTable::getId,
                table -> {
                    // Check if table is occupied today
                    if (table.getStatus() == RestaurantTable.TableStatus.OCCUPIED) {
                        if (date.equals(LocalDate.now())) {
                            boolean conflictWithOccupied = false;
                            try {
                                com.web.web.Entity.TableOrder order = tableOrderRepository
                                        .findByTableIdAndStatus(table.getId(),
                                                com.web.web.Entity.TableOrder.OrderStatus.OPEN)
                                        .orElse(null);
                                if (order != null && order.getEntryTime() != null && !order.getEntryTime().isEmpty()) {
                                    LocalTime entryTime = LocalTime.parse(order.getEntryTime());
                                    if (isTimeConflict(entryTime, time)) {
                                        conflictWithOccupied = true;
                                    }
                                } else {
                                    if (time.isBefore(LocalTime.now().plusMinutes(150))) {
                                        conflictWithOccupied = true;
                                    }
                                }
                            } catch (Exception e) {
                                if (time.isBefore(LocalTime.now().plusMinutes(150))) {
                                    conflictWithOccupied = true;
                                }
                            }
                            if (conflictWithOccupied) {
                                return false;
                            }
                        }
                    }
                    // Check conflict với booking hiện có
                    return activeBookings.stream().noneMatch(b -> b.getTable() != null &&
                            b.getTable().getId().equals(table.getId()) &&
                            isTimeConflict(b.getBookingTime(), time));
                }));
    }

    /**
     * Check 2 thời gian booking có trùng slot không (Khóa bàn trong 149 phút =
     * 2h29m)
     */
    private boolean isTimeConflict(LocalTime existingTime, LocalTime newTime) {
        LocalTime existingEnd = existingTime.plusMinutes(149);
        LocalTime newEnd = newTime.plusMinutes(149);

        // Trùng nhau khi có sự giao nhau giữa [existingTime, existingEnd] và [newTime,
        // newEnd]
        return existingTime.isBefore(newEnd) && existingEnd.isAfter(newTime);
    }

    // ══════════════════════════════════════════
    // CẬP NHẬT TRẠNG THÁI
    // ══════════════════════════════════════════

    /** Nhân viên đánh dấu khách đã đến */
    @Transactional
    public BookingDTO checkInBooking(Long id) {
        Booking booking = findBookingOrThrow(id);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ có thể check-in đơn ở trạng thái Đã đặt");
        }
        booking.setStatus(BookingStatus.CHECKED_IN);
        return toDTO(bookingRepository.save(booking));
    }

    /** Nhân viên đánh dấu hoàn thành (đã thanh toán) — trả bàn về EMPTY */
    @Transactional
    public BookingDTO completeBooking(Long id) {
        Booking booking = findBookingOrThrow(id);
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new RuntimeException("Chỉ có thể hoàn thành đơn ở trạng thái Khách đã đến");
        }
        booking.setStatus(BookingStatus.COMPLETED);

        // Trả bàn về trạng thái trống
        RestaurantTable table = booking.getTable();
        if (table != null) {
            table.setStatus(RestaurantTable.TableStatus.EMPTY);
            tableRepository.save(table);
        }

        return toDTO(bookingRepository.save(booking));
    }

    // ══════════════════════════════════════════
    // HỦY BOOKING
    // ══════════════════════════════════════════

    /** User tự hủy (chỉ khi CONFIRMED, trước giờ hẹn) */
    @Transactional
    public BookingDTO cancelBookingByUser(Long id, String username) {
        Booking booking = findBookingOrThrow(id);

        if (!booking.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn đặt bàn này");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ có thể hủy đơn ở trạng thái Đã đặt");
        }

        // Kiểm tra chưa quá giờ hẹn
        LocalDateTime bookingDateTime = LocalDateTime.of(booking.getBookingDate(), booking.getBookingTime());
        if (LocalDateTime.now().isAfter(bookingDateTime)) {
            throw new RuntimeException("Không thể hủy đơn đặt bàn đã qua giờ hẹn");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason("Khách hàng tự hủy");
        return toDTO(bookingRepository.save(booking));
    }

    /** Nhân viên hủy (bắt buộc điền lý do) */
    @Transactional
    public BookingDTO cancelBookingByStaff(Long id, String cancelReason) {
        Booking booking = findBookingOrThrow(id);

        if (booking.getStatus() == BookingStatus.COMPLETED ||
                booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Không thể hủy đơn đã hoàn thành hoặc đã hủy");
        }
        if (cancelReason == null || cancelReason.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng điền lý do hủy đơn đặt bàn");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason(cancelReason.trim());

        // Trả bàn về trạng thái trống nếu đang khóa
        RestaurantTable table = booking.getTable();
        if (table != null && table.getStatus() == RestaurantTable.TableStatus.OCCUPIED) {
            table.setStatus(RestaurantTable.TableStatus.EMPTY);
            tableRepository.save(table);
        }

        return toDTO(bookingRepository.save(booking));
    }

    /** Nhân viên đánh dấu no-show (không đến) */
    @Transactional
    public BookingDTO noShowBooking(Long id) {
        Booking booking = findBookingOrThrow(id);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ có thể đánh dấu không đến cho đơn ở trạng thái Đã đặt");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason("Khách không đến (No-show)");

        // Trả bàn
        RestaurantTable table = booking.getTable();
        if (table != null && table.getStatus() == RestaurantTable.TableStatus.OCCUPIED) {
            table.setStatus(RestaurantTable.TableStatus.EMPTY);
            tableRepository.save(table);
        }

        return toDTO(bookingRepository.save(booking));
    }

    // ══════════════════════════════════════════
    // XÓA BOOKING
    // ══════════════════════════════════════════

    /** Admin xóa đơn đặt bàn khỏi hệ thống */
    @Transactional
    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new RuntimeException("Đơn đặt bàn không tồn tại");
        }
        bookingRepository.deleteById(id);
    }

    // ══════════════════════════════════════════
    // HELPER
    // ══════════════════════════════════════════

    private Booking findBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn đặt bàn không tồn tại"));
    }

    /** Chuyển Entity → DTO (bao gồm tableName + areaName) */
    private BookingDTO toDTO(Booking booking) {
        String tableName = null;
        String areaName = null;

        if (booking.getTable() != null) {
            tableName = booking.getTable().getName();
            if (booking.getTable().getArea() != null) {
                areaName = booking.getTable().getArea().getName();
            }
        }

        BookingDTO dto = new BookingDTO(
                booking.getId(),
                booking.getFullName(),
                booking.getPhoneNumber(),
                booking.getBookingDate(),
                booking.getBookingTime(),
                booking.getNumberOfGuests(),
                booking.getSpecialRequests(),
                booking.getTable() != null ? booking.getTable().getId() : null,
                tableName,
                areaName,
                booking.getCancelReason(),
                booking.getCreatedAt(),
                booking.getStatus().name(),
                booking.getUser().getUsername());

        // Fetch Order items
        List<Order> orders = orderRepository.findByBookingId(booking.getId());
        List<OrderItemDTO> orderedItems = new ArrayList<>();
        String paymentMethod = null;
        if (orders != null && !orders.isEmpty()) {
            for (Order order : orders) {
                if (order.getOrderItems() != null) {
                    for (OrderItem item : order.getOrderItems()) {
                        OrderItemDTO itemDto = new OrderItemDTO();
                        itemDto.setId(item.getId());
                        itemDto.setProductId(item.getProduct().getId());
                        itemDto.setProductName(item.getProduct().getName());
                        itemDto.setQuantity(item.getQuantity());
                        itemDto.setProductImage(item.getProduct().getImg());
                        itemDto.setUnitPrice(item.getUnitPrice());
                        itemDto.setSubtotal(item.getSubtotal());
                        orderedItems.add(itemDto);
                    }
                }
                if (paymentMethod == null) {
                    Payment payment = paymentRepository.findByOrderId(order.getId());
                    if (payment != null && payment.getPaymentMethod() != null) {
                        paymentMethod = payment.getPaymentMethod().name();
                    }
                }
            }
        }
        dto.setOrderedItems(orderedItems);
        dto.setPaymentMethod(paymentMethod);
        return dto;
    }
}
