package com.web.web.Controller;

import com.web.web.Dto.ChatRequest;
import com.web.web.Dto.ChatResponse;
import com.web.web.Dto.ProductDTO;
import com.web.web.Service.ChatbotService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chatWithBot(@Valid @RequestBody ChatRequest request) {
        try {
            Map<String, Object> serviceResponse = chatbotService.processChatQuery(request.getMessage());

            // Thêm kiểm tra null để an toàn (tùy chọn, nhưng tốt hơn)
            @SuppressWarnings("unchecked") // Tránh warning unchecked cast
            List<ProductDTO> products = (List<ProductDTO>) serviceResponse.get("products");
            if (products == null) {
                products = new java.util.ArrayList<>();
            }

            ChatResponse response = new ChatResponse(
                    (String) serviceResponse.get("reply"),
                    (String) serviceResponse.get("message"),
                    products
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ChatResponse("Lỗi: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ChatResponse("Lỗi server khi xử lý câu hỏi: " + e.getMessage()));
        }
    }
}
