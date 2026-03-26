package com.web.web.Controller;

import com.web.web.Dto.SePayWebhookDTO;
import com.web.web.Service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/sepay")
public class SePayWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(SePayWebhookController.class);
    private final OrderService orderService;

    public SePayWebhookController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> receiveWebhook(@RequestBody SePayWebhookDTO webhookData) {
        logger.info("Received SePay webhook: {}", webhookData);

        if (!"in".equalsIgnoreCase(webhookData.getTransferType())) {
            return ResponseEntity.ok("Not a receive transaction");
        }

        // Logic to extract Order ID from content
        // Assuming the content format is like "ORD123" or has "ORD" followed by numbers
        Long orderId = extractOrderId(webhookData.getContent());

        if (orderId != null) {
            try {
                logger.info("Updating payment status for Order ID: {}", orderId);
                orderService.updatePaymentStatus(orderId, "PAID");
                // Optional: You might also want to update Order Status to CONFIRMED
                orderService.updateOrderStatus(orderId, "CONFIRMED");
                return ResponseEntity.ok("Order updated successfully");
            } catch (Exception e) {
                logger.error("Error updating order {}: {}", orderId, e.getMessage());
                return ResponseEntity.status(500).body("Error updating order");
            }
        }

        return ResponseEntity.badRequest().body("Order ID not found in content");
    }

    private Long extractOrderId(String content) {
        if (content == null)
            return null;

        // Match ORD followed by digits (e.g., ORD123)
        Pattern pattern = Pattern.compile("ORD(\\d+)");
        Matcher matcher = pattern.matcher(content.toUpperCase());

        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }

        // Fallback: search for any sequence of numbers if no ORD prefix
        pattern = Pattern.compile("(\\d+)");
        matcher = pattern.matcher(content);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }

        return null;
    }
}
