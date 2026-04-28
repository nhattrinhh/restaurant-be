package com.web.web.Dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class BookingDTO {

    private Long id;

    @NotBlank(message = "Tên đầy đủ không được để trống")
    @Size(min = 2, max = 100, message = "Tên đầy đủ phải từ 2 đến 100 ký tự")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @NotNull(message = "Ngày đặt bàn không được để trống")
    private LocalDate bookingDate;

    @NotNull(message = "Giờ đặt bàn không được để trống")
    private LocalTime bookingTime;

    @AssertTrue(message = "Thời gian đặt bàn phải lớn hơn thời điểm hiện tại")
    public boolean isBookingDateTimeValid() {
        if (bookingDate == null || bookingTime == null)
            return true;
        LocalDateTime bookingDateTime = LocalDateTime.of(bookingDate, bookingTime);
        return !bookingDateTime.isBefore(LocalDateTime.now());
    }

    @AssertTrue(message = "Giờ đặt bàn phải trong khung giờ 09:00 - 23:00")
    public boolean isBookingTimeValid() {
        if (bookingTime == null)
            return true;
        return !bookingTime.isBefore(LocalTime.of(9, 0)) &&
                !bookingTime.isAfter(LocalTime.of(23, 0));
    }

    @NotNull(message = "Số lượng khách không được để trống")
    @Min(value = 1, message = "Số lượng khách phải từ 1 trở lên")
    @Max(value = 50, message = "Số lượng khách tối đa là 50")
    private Integer numberOfGuests;

    @Size(max = 500, message = "Yêu cầu đặc biệt không được vượt quá 500 ký tự")
    private String specialRequests;

    // Thông tin bàn — FE gửi tableId, BE trả về cả tableName + areaName
    @NotNull(message = "Vui lòng chọn bàn")
    private Long tableId;
    private String tableName;  // Tên bàn (VD: "Bàn 01") — chỉ dùng cho response
    private String areaName;   // Tên khu vực (VD: "PHÒNG VIP") — chỉ dùng cho response

    // Lý do hủy (bắt buộc khi NV hủy)
    private String cancelReason;

    // Metadata
    private LocalDateTime createdAt;

    @Pattern(regexp = "^(CONFIRMED|CHECKED_IN|COMPLETED|CANCELLED)$",
            message = "Trạng thái phải là CONFIRMED, CHECKED_IN, COMPLETED hoặc CANCELLED")
    private String status;

    private String username;

    private List<OrderItemDTO> orderedItems;

    // ── Constructors ──

    public BookingDTO() {
    }

    public BookingDTO(Long id, String fullName, String phoneNumber,
                      LocalDate bookingDate, LocalTime bookingTime,
                      Integer numberOfGuests, String specialRequests,
                      Long tableId, String tableName, String areaName,
                      String cancelReason,
                      LocalDateTime createdAt, String status, String username) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.numberOfGuests = numberOfGuests;
        this.specialRequests = specialRequests;
        this.tableId = tableId;
        this.tableName = tableName;
        this.areaName = areaName;
        this.cancelReason = cancelReason;
        this.createdAt = createdAt;
        this.status = status;
        this.username = username;
    }

    // ── Getters & Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public LocalTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalTime bookingTime) { this.bookingTime = bookingTime; }

    public Integer getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(Integer numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<OrderItemDTO> getOrderedItems() { return orderedItems; }
    public void setOrderedItems(List<OrderItemDTO> orderedItems) { this.orderedItems = orderedItems; }
}
