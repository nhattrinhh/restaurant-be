package com.web.web.Dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BookingDTO {

    private Long id;

    @NotBlank(message = "Tên đầy đủ không được để trống")
    @Size(min = 2, max = 100, message = "Tên đầy đủ phải từ 2 đến 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z\\s\\-']+$", message = "Tên đầy đủ chỉ được chứa chữ cái, khoảng trắng, dấu gạch ngang hoặc dấu nháy đơn")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(\\+84|0)[0-9]{9,12}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @NotNull(message = "Ngày đặt bàn không được để trống")
    @FutureOrPresent(message = "Ngày đặt bàn phải là ngày hiện tại hoặc trong tương lai")
    private LocalDate bookingDate;

    @NotNull(message = "Giờ đặt bàn không được để trống")
    private LocalTime bookingTime;

    @NotNull(message = "Số lượng khách không được để trống")
    @Positive(message = "Số lượng khách phải là số dương")
    @Max(value = 50, message = "Số lượng khách tối đa là 50")
    private Integer numberOfGuests;

    @Size(max = 255, message = "Khu vực không được vượt quá 255 ký tự")
    private String area;

    @Size(max = 500, message = "Yêu cầu đặc biệt không được vượt quá 500 ký tự")
    private String specialRequests;

    private LocalDateTime createdAt;


    @Pattern(regexp = "^(PENDING|CONFIRMED|CANCELLED|CANCEL_REQUESTED)$",
            message = "Trạng thái phải là PENDING, CONFIRMED, CANCELLED hoặc CANCEL_REQUESTED")
    private String status;


    private String username;

    public BookingDTO() {}

    public BookingDTO(Long id, String fullName, String phoneNumber, LocalDate bookingDate,
                      LocalTime bookingTime, Integer numberOfGuests, String area,
                      String specialRequests, LocalDateTime createdAt, String status, String username) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.numberOfGuests = numberOfGuests;
        this.area = area;
        this.specialRequests = specialRequests;
        this.createdAt = createdAt;
        this.status = status;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public Integer getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(Integer numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
