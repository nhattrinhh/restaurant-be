# Backend Project Rules

> **Tech thực tế**: Spring Boot 3 + Java 17 (không phải NestJS + TypeScript).
> Xem `docs/decisions/BE-001-spring-boot-over-nestjs.md` để biết lý do.

## Tech Stack

| Technology | Version / Chi Tiết |
|------------|-------------------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.7 |
| ORM | Spring Data JPA (Hibernate) |
| Database | MySQL 8.x |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| OAuth2 | Spring Boot OAuth2 Client (Google) |
| File Storage | AWS SDK v2 → Cloudflare R2 |
| Validation | Spring Boot Starter Validation |
| Lombok | Reduce boilerplate |
| Payment | SePay webhook |

---

## 1. Layer Structure

```
com.web.web/
├── Config/         # Cấu hình cross-cutting
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   ├── JwtConfig.java
│   └── S3Config.java
│
├── Controller/     # HTTP endpoints (18 controllers)
│   ├── AuthController.java
│   ├── UserController.java
│   ├── ProductController.java
│   ├── CategoryController.java
│   ├── ProductTypeController.java
│   ├── CartController.java
│   ├── OrderController.java
│   ├── BookingController.java
│   ├── RestaurantTableController.java
│   ├── TableAreaController.java
│   ├── TableOrderController.java
│   ├── TableInvoiceController.java
│   ├── PromotionController.java
│   ├── NewsController.java
│   ├── ChatbotController.java
│   ├── UploadController.java
│   ├── SePayWebhookController.java
│   └── StatisticsController.java
│
├── Dto/            # Request/Response DTOs
│   ├── request/    # CreateProductRequest, LoginRequest...
│   └── response/   # ProductResponse, OrderResponse...
│
├── Entity/         # JPA entities (17 entities)
│   ├── User.java, Role.java
│   ├── Product.java, Category.java, ProductType.java
│   ├── Cart.java, CartItem.java
│   ├── Order.java, OrderItem.java, Payment.java
│   ├── Booking.java
│   ├── RestaurantTable.java, TableArea.java
│   ├── TableOrder.java, TableInvoice.java
│   ├── Promotion.java
│   └── News.java
│
├── Exception/      # Custom exceptions
│   ├── GlobalExceptionHandler.java  (@RestControllerAdvice)
│   └── ResourceNotFoundException.java
│
├── Repository/     # Spring Data JPA interfaces
│   └── [Entity]Repository.java (extends JpaRepository)
│
├── Response/       # Response wrapper
│   └── ApiResponse.java
│
├── Security/       # Spring Security
│   ├── JwtAuthFilter.java
│   ├── JwtUtil.java
│   └── UserDetailsServiceImpl.java
│
└── Service/        # Business logic (18 services)
    ├── UserService.java
    ├── ProductService.java
    ├── CartService.java
    ├── OrderService.java
    ├── BookingService.java
    ├── [...]
    └── R2Service.java
```

---

## 2. Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Package | Viết hoa (legacy) | `Controller/`, `Service/` |
| Classes | PascalCase | `UserService`, `CartController` |
| Methods | camelCase | `findById()`, `createOrder()` |
| Variables | camelCase | `userId`, `cartItems` |
| Constants | UPPER_SNAKE_CASE | `MAX_FILE_SIZE` |
| DB columns | snake_case | `created_at`, `user_id` |
| API paths | kebab-case | `/api/v1/table-areas` |

---

## 3. Layer Responsibilities

### Controller Layer
```java
// ✅ Chỉ làm: validate input + gọi Service + format response
@PostMapping("/cart/items")
public ResponseEntity<?> addItem(
        @Valid @RequestBody AddItemRequest req,
        @AuthenticationPrincipal UserDetails user) {
    return ResponseEntity.ok(cartService.addItem(user.getUsername(), req));
}

// ❌ KHÔNG: business logic trong controller
```

### Service Layer
```java
// ✅ Toàn bộ business logic
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepo;
    private final ProductRepository productRepo;

    @Transactional
    public CartResponse addItem(String email, AddItemRequest req) {
        // Validate, transform, persist
    }
}

// ❌ KHÔNG: query SQL trực tiếp, gọi Controller khác
```

### Repository Layer
```java
// ✅ Chỉ: Spring Data JPA methods + @Query phức tạp
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt > :date")
    List<Order> findRecentByStatus(@Param("status") String status, @Param("date") LocalDateTime date);
}

// ❌ KHÔNG: business logic, gọi Service
```

---

## 4. Code Patterns

### Response Wrapper

```java
// ApiResponse wrapper
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private Object error;

    public static <T> ApiResponse<T> success(T data) { ... }
    public static ApiResponse<?> error(String code, String message) { ... }
}
```

### Error Handling

```java
// ✅ Custom exception
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

// ✅ Global handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404)
            .body(ApiResponse.error("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        // Return validation errors
    }
}
```

### Validation (DTOs)

```java
// ✅ Bean Validation trong DTO
public class CreateProductRequest {
    @NotBlank(message = "Tên sản phẩm không được trống")
    private String name;

    @NotNull
    @Min(value = 0, message = "Giá phải >= 0")
    private BigDecimal price;
}

// ✅ @Valid trong controller
public ResponseEntity<?> create(@Valid @RequestBody CreateProductRequest req) { ... }
```

### Transaction

```java
// ✅ @Transactional cho operations ghi phức tạp (e.g. checkout)
@Transactional
public OrderResponse checkout(Long userId, CheckoutRequest req) {
    // 1. Get cart
    // 2. Create order
    // 3. Create order items (snapshot)
    // 4. Create payment
    // 5. Clear cart
    // Nếu bất kỳ bước nào lỗi → rollback tất cả
}
```

### Security (JWT)

```java
// ✅ Lấy user từ SecurityContext trong controller/service
@GetMapping("/me")
public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
    return ResponseEntity.ok(userService.findByEmail(userDetails.getUsername()));
}

// ✅ Admin-only endpoint
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<?> delete(@PathVariable Long id) { ... }
```

---

## 5. Anti-Patterns (DON'T)

| ❌ ĐỪNG | ✅ NÊN |
|---------|--------|
| Business logic trong Controller | Logic trong Service |
| SQL/JPQL trong Service | Dùng Repository method |
| Commit file `.env` | Dùng `.env.example` |
| Lưu plain text password | Dùng BCryptPasswordEncoder |
| Hardcode URL/secret | Dùng `@Value` / env variables |
| Sửa schema DB trực tiếp | Sửa qua initdb script |
| Link cart/order → products sai | Xem DATABASE.md cho relations thực tế |
| Logic trong Controller | Service chịu trách nhiệm |

---

## 6. Git Workflow

### Branch Naming

```
feature/cart-merge-guest
fix/booking-time-validation
refactor/order-checkout-transaction
```

### Commit Messages

```
feat: add SePay webhook handler
fix: correct cart quantity update logic
refactor: extract checkout service
docs: update API spec for booking
```

---

## 7. Environment Variables

```bash
# Database
DB_HOST=localhost
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=...
DB_NAME=platia_restaurant

# JWT
JWT_SECRET=...
JWT_EXPIRATION=86400000   # 24h ms

# Cloudflare R2 (S3-compatible)
AWS_ACCESS_KEY_ID=...
AWS_SECRET_ACCESS_KEY=...
AWS_REGION=auto
AWS_S3_BUCKET=...
AWS_ENDPOINT_URL=https://....r2.cloudflarestorage.com

# SePay
SEPAY_API_KEY=...
```

> ❌ **KHÔNG commit `.env`** — dùng `.env.example` làm template

---

## 8. Commands

```bash
# Chạy development
cd restaurant-be
./mvnw spring-boot:run

# Build
./mvnw clean package -DskipTests

# Test
./mvnw test

# Docker
docker-compose up -d
```