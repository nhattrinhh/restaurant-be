package com.web.web.Controller;

import com.web.web.Dto.CookingInstructionResponse;
import com.web.web.Dto.SetCookingInstructionRequest;
import com.web.web.Entity.User;
import com.web.web.Exception.DataNotFoundException;
import com.web.web.Service.CookingInstructionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products/{productId}/instruction")
public class CookingInstructionController {

    @Autowired
    private CookingInstructionService instructionService;

    @GetMapping
    public ResponseEntity<CookingInstructionResponse> getInstruction(@PathVariable Long productId) throws DataNotFoundException {
        return ResponseEntity.ok(instructionService.getByProductId(productId));
    }

    @PutMapping
    public ResponseEntity<CookingInstructionResponse> setInstruction(
            @PathVariable Long productId,
            @RequestBody SetCookingInstructionRequest request,
            @AuthenticationPrincipal User user) throws DataNotFoundException {
        return ResponseEntity.ok(instructionService.setInstruction(productId, request, user.getId()));
    }
}
