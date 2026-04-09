# Backend — Project Status

> Cập nhật file này mỗi khi hoàn thành một module hoặc phát sinh issue mới.
> Format ngày: `YYYY-MM-DD`

---

## Tổng Quan Tiến Độ

| Layer | Trạng thái |
|-------|-----------|
| Config / Security | ✅ Hoàn thành |
| Entity (17 entities) | ✅ Hoàn thành |
| Repository (Spring Data JPA) | ✅ Hoàn thành |
| Service layer | 🔄 90% — còn edge case |
| Controller layer (18 controllers) | ✅ Hoàn thành |
| Exception handling (GlobalExceptionHandler) | ✅ Hoàn thành |

---

## Controllers — Trạng Thái Chi Tiết

| Controller | Status | Ghi chú |
|------------|--------|---------|
| `AuthController` | ✅ Done | JWT + OAuth2 (Google) |
| `UserController` | ✅ Done | CRUD user, profile, change password |
| `ProductController` | ✅ Done | CRUD sản phẩm + upload ảnh R2 |
| `CategoryController` | ✅ Done | |
| `ProductTypeController` | ✅ Done | |
| `CartController` | ✅ Done | add/update/remove/clear |
| `OrderController` | ✅ Done | checkout, lịch sử, admin quản lý |
| `BookingController` | ✅ Done | đặt bàn, kiểm tra availability |
| `RestaurantTableController` | ✅ Done | |
| `TableAreaController` | ✅ Done | |
| `TableOrderController` | ✅ Done | |
| `TableInvoiceController` | ✅ Done | |
| `PromotionController` | ✅ Done | có PromotionScheduler chạy nền |
| `NewsController` | ✅ Done | |
| `ChatbotController` | ✅ Done | gọi external AI API (OKHttp) |
| `UploadController` | ✅ Done | Cloudflare R2 (S3-compatible) |
| `SePayWebhookController` | ✅ Done | webhook thanh toán, verify API key |
| `StatisticsController` | ✅ Done | thống kê admin |

---

## Services — Vấn Đề / Cần Xem Lại

| Service | Issue | Priority |
|---------|-------|----------|
| `CartService` | Chưa handle race condition khi đồng thời add item | 🟡 Medium |
| `OrderService` | Kiểm tra lại transaction rollback khi checkout thất bại | 🟡 Medium |
| `BookingService` | Xem lại logic kiểm tra time slot trùng | 🟡 Medium |
| `PromotionService` | Interface + `PromotionServiceImpl` — cần refactor nhất quán | 🟢 Low |
| `ChatbotService` | Context window giới hạn khi menu quá dài | 🟢 Low |

---

## Đang Làm / Backlog

- [ ] Viết unit test cho `CartService` (addItem, removeItem, mergeGuestCart)
- [ ] Viết unit test cho `OrderService` (checkout transaction)
- [ ] Kiểm tra security: tất cả endpoint admin đã có `@PreAuthorize` chưa
- [ ] Validate webhook signature từ SePay (hiện chỉ check API key)

---

## Đã Hoàn Thành Gần Đây

- [x] CartService: add/update/remove/clear cart items
- [x] OrderService: checkout flow với snapshot product data
- [x] SePayWebhookController: xử lý callback thanh toán
- [x] R2Service: upload ảnh lên Cloudflare R2
- [x] GlobalExceptionHandler: tập trung xử lý exception

---

## Known Issues

| Issue | Mô tả | Workaround |
|-------|-------|------------|
| `PromotionService` interface | Có cả interface + impl, không nhất quán với các service khác | Dùng `PromotionServiceImpl` trực tiếp |
| `.env` committed | File `.env` đang trong repo (chứa secret thật) | Cần xóa khỏi git history, dùng `.env.example` |

---

## Environment / Setup

```bash
# Chạy local
cd restaurant-be
./mvnw spring-boot:run

# DB: MySQL 8.x, DB name: platia_restaurant
# Port: 8080
# API Base: http://localhost:8080/api/v1
```
