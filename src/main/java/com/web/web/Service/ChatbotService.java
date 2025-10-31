package com.web.web.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.web.Dto.ProductDTO;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0");

    private final ProductService productService;
    private final ProductTypeService productTypeService;
    private final CategoryService categoryService;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Autowired
    public ChatbotService(ProductService productService, ProductTypeService productTypeService,
                          CategoryService categoryService, OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.productService = productService;
        this.productTypeService = productTypeService;
        this.categoryService = categoryService;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> processChatQuery(String userQuery) {
        Map<String, Object> response = new HashMap<>();

        if (userQuery == null || userQuery.trim().isEmpty()) {
            logger.warn("Câu hỏi người dùng rỗng hoặc null");
            response.put("message", "Câu hỏi không được để trống");
            response.put("reply", "");
            response.put("products", new ArrayList<>()); // Thêm trường products
            return response;
        }

        try {
            logger.info("Xử lý câu hỏi: {}", userQuery);
            List<ProductDTO> products = fetchRelevantProducts(userQuery);
            String productSummary = generateProductSummary(products);
            String prompt = String.format(
                    "Bạn là một chatbot tư vấn món ăn. Dựa trên câu hỏi của người dùng và danh sách sản phẩm dưới đây, hãy đưa ra gợi ý món ăn hoặc loại món ăn phù hợp. Trả lời bằng tiếng Việt, ngắn gọn, thân thiện và tự nhiên, như một nhân viên nhà hàng nhiệt tình. Nếu gợi ý sản phẩm cụ thể, chỉ bao gồm tên sản phẩm trong phản hồi, không bao gồm ID. Nếu không tìm thấy món phù hợp, trả lời lịch sự.\n\n" +
                            "Câu hỏi người dùng: %s\n\n" +
                            "Danh sách sản phẩm:\n%s\n\n" +
                            "Trả lời:", userQuery, productSummary);

            String botReply = callGroqApi(prompt);

            response.put("message", products.isEmpty() ? "Không tìm thấy sản phẩm phù hợp" : "Tư vấn thành công");
            response.put("reply", botReply);
            response.put("products", products); // Thêm danh sách sản phẩm vào phản hồi
            logger.info("Phản hồi chatbot: {}", botReply);
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý câu hỏi: {}", e.getMessage(), e);
            response.put("message", "Lỗi khi xử lý câu hỏi: " + e.getMessage());
            response.put("reply", "");
            response.put("products", new ArrayList<>());
        }

        return response;
    }

    private List<ProductDTO> fetchRelevantProducts(String userQuery) {
        // Lấy toàn bộ sản phẩm AVAILABLE 1 lần để giảm số lần query/phép lọc lặp
        List<ProductDTO> allAvailable = productService.getAllProducts().stream()
                .filter(p -> "AVAILABLE".equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList());

        // Nếu query rỗng -> random 5 món AVAILABLE
        if (userQuery == null || userQuery.trim().isEmpty()) {
            Collections.shuffle(allAvailable);
            return allAvailable.stream().limit(5).collect(Collectors.toList());
        }

        String lowerQuery = userQuery.toLowerCase(Locale.ROOT).trim();

        // Phát hiện ý định tìm kiếm đơn giản
        boolean isSearchIntent =
                lowerQuery.contains("tìm") ||
                        lowerQuery.contains("món") ||
                        lowerQuery.contains("ăn gì") ||
                        lowerQuery.contains("nhà hàng") ||
                        lowerQuery.contains("gợi ý") ||
                        lowerQuery.contains("tư vấn") ||
                        lowerQuery.contains("có")||
                        lowerQuery.contains("không");

        // Chuẩn hoá từ khoá để tìm
        String searchTerm = lowerQuery
                .replaceAll("\\b(tìm|món|ăn|gì|nhà hàng|gợi ý|tư vấn|các|của|nào|nhất|có|không|1|vài|một|tôi|mình||về|đề xuất|)\\b", "") // bỏ stop-words cơ bản
                .replaceAll("\\s+", " ")
                .replaceAll("[?]", "")
                .trim();

        // Dùng map theo id để loại trùng (giữ món đầu tiên tìm thấy)
        Map<Long, ProductDTO> dedupById = new LinkedHashMap<>();

        if (isSearchIntent) {
            // 1) Tên sản phẩm
            if (!searchTerm.isEmpty()) {
                try {
                    List<ProductDTO> byName = productService.searchProductsByName(searchTerm).stream()
                            .filter(p -> "AVAILABLE".equalsIgnoreCase(p.getStatus()))
                            .collect(Collectors.toList());
                    byName.forEach(p -> dedupById.putIfAbsent(p.getId(), p));
                    logger.info("Tìm thấy {} sản phẩm theo tên", byName.size());
                } catch (IllegalArgumentException e) {
                    logger.warn("Lỗi tìm theo tên '{}': {}", searchTerm, e.getMessage());
                }
            }

            // 2) Theo loại sản phẩm
            try {
                // Tìm loại có chứa từ khoá (nếu rỗng thì bỏ qua)
                if (!searchTerm.isEmpty()) {
                    productTypeService.getAllProductTypes().stream()
                            .filter(pt -> pt.getName() != null &&
                                    pt.getName().toLowerCase(Locale.ROOT).contains(searchTerm))
                            .findFirst()
                            .ifPresent(pt -> {
                                List<ProductDTO> byType = productService.getProductsByProductTypeId(pt.getId()).stream()
                                        .filter(p -> "AVAILABLE".equalsIgnoreCase(p.getStatus()))
                                        .collect(Collectors.toList());
                                byType.forEach(p -> dedupById.putIfAbsent(p.getId(), p));
                                logger.info("Tìm thấy {} sản phẩm theo loại '{}'", byType.size(), pt.getName());
                            });
                }
            } catch (Exception e) {
                logger.warn("Lỗi tìm theo loại '{}': {}", searchTerm, e.getMessage());
            }

            // 3) Theo danh mục
            try {
                if (!searchTerm.isEmpty()) {
                    categoryService.getAllCategories().stream()
                            .filter(c -> c.getName() != null &&
                                    c.getName().toLowerCase(Locale.ROOT).contains(searchTerm))
                            .findFirst()
                            .ifPresent(cat -> {
                                List<ProductDTO> byCat = productService.getProductsByCategoryId(cat.getId()).stream()
                                        .filter(p -> "AVAILABLE".equalsIgnoreCase(p.getStatus()))
                                        .collect(Collectors.toList());
                                byCat.forEach(p -> dedupById.putIfAbsent(p.getId(), p));
                                logger.info("Tìm thấy {} sản phẩm theo danh mục '{}'", byCat.size(), cat.getName());
                            });
                }
            } catch (Exception e) {
                logger.warn("Lỗi tìm theo danh mục '{}': {}", searchTerm, e.getMessage());
            }
        }

        List<ProductDTO> collected = new ArrayList<>(dedupById.values());

        // Nếu sau khi tìm vẫn rỗng -> fallback ngẫu nhiên từ allAvailable
        if (collected.isEmpty()) {
            logger.info("Không tìm thấy theo từ khoá '{}', trả về 5 sản phẩm ngẫu nhiên", lowerQuery);
            Collections.shuffle(allAvailable);
            return allAvailable.stream().limit(5).collect(Collectors.toList());
        }

        // Xáo trộn và giới hạn 5
        Collections.shuffle(collected);
        return collected.stream().limit(5).collect(Collectors.toList());
    }


    private String generateProductSummary(List<ProductDTO> products) {
        if (products.isEmpty()) {
            return "Không có sản phẩm nào đang có sẵn.";
        }

        return products.stream()
                .limit(5) // Giới hạn tối đa 5 sản phẩm
                .map(p -> {
                    String name = p.getName() != null ? p.getName() : "Không xác định";
                    String productTypeName = p.getProductTypeName() != null ? p.getProductTypeName() : "Không xác định";
                    String categoryName = p.getCategoryName() != null ? p.getCategoryName() : "Không có";
                    String status = p.getStatus() != null ? p.getStatus() : "Không xác định";
                    String originalPrice = DECIMAL_FORMAT.format(p.getOriginalPrice());
                    String discountedPrice = p.getDiscountedPrice() >= 0 ? DECIMAL_FORMAT.format(p.getDiscountedPrice()) : "Không có";
                    return String.format("Tên: %s, Loại: %s, Danh mục: %s, Giá: %s VND, Giá giảm: %s VND, Trạng thái: %s",
                            name, productTypeName, categoryName, originalPrice, discountedPrice, status);
                })
                .collect(Collectors.joining("\n"));
    }

    private String callGroqApi(String prompt) throws IOException {
        logger.info("Gửi yêu cầu tới Groq API với prompt: {}", prompt);

        // Tạo JSON body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.1-8b-instant");
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);
        requestBody.put("messages", Arrays.asList(userMsg));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 200);

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            logger.error("Lỗi khi tạo JSON body: {}", e.getMessage());
            throw new IOException("Lỗi khi tạo JSON body: " + e.getMessage());
        }

        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.get("application/json; charset=utf-8")
        );

        Request groqRequest = new Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + groqApiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(groqRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorMsg = response.body() != null ? response.body().string() : response.message();
                logger.error("Lỗi từ Groq API: {} - {}", response.code(), errorMsg);
                if (response.code() == 401) {
                    throw new IOException("API key không hợp lệ hoặc không có quyền truy cập");
                } else if (response.code() == 429) {
                    throw new IOException("Vượt quá giới hạn yêu cầu API");
                } else {
                    throw new IOException("Lỗi từ Groq API: " + errorMsg);
                }
            }

            String responseBody = response.body().string();
            JsonNode root;
            try {
                root = objectMapper.readTree(responseBody);
            } catch (Exception e) {
                logger.error("Lỗi khi phân tích JSON phản hồi: {}", e.getMessage());
                throw new IOException("Phản hồi từ Groq API không hợp lệ: " + e.getMessage());
            }

            if (root == null || !root.has("choices") || !root.get("choices").isArray() || root.get("choices").size() == 0) {
                logger.warn("Phản hồi từ Groq API không hợp lệ hoặc rỗng");
                throw new IOException("Phản hồi từ Groq API không hợp lệ hoặc rỗng");
            }

            JsonNode message = root.get("choices").get(0).get("message");
            if (message == null || !message.has("content")) {
                logger.warn("Không tìm thấy nội dung trong phản hồi từ Groq API");
                return "Xin lỗi, hiện chưa có gợi ý phù hợp.";
            }

            String reply = message.get("content").asText();
            logger.info("Phản hồi từ Groq API: {}", reply);
            return reply;
        }
    }
}
