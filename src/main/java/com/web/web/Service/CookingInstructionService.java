package com.web.web.Service;

import com.web.web.Dto.CookingInstructionResponse;
import com.web.web.Dto.SetCookingInstructionRequest;
import com.web.web.Entity.CookingInstruction;
import com.web.web.Entity.Product;
import com.web.web.Entity.User;
import com.web.web.Exception.DataNotFoundException;
import com.web.web.Repository.CookingInstructionRepository;
import com.web.web.Repository.ProductRepository;
import com.web.web.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CookingInstructionService {

    @Autowired
    private CookingInstructionRepository cookingInstructionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public CookingInstructionResponse getByProductId(Long productId) throws DataNotFoundException {
        CookingInstruction instruction = cookingInstructionRepository.findByProductId(productId)
                .orElseThrow(() -> new DataNotFoundException("Cooking instruction not found for this product"));
        return mapToResponse(instruction);
    }

    @Transactional
    public CookingInstructionResponse setInstruction(Long productId, SetCookingInstructionRequest request, Long userId)
            throws DataNotFoundException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Product not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        Optional<CookingInstruction> existingOpt = cookingInstructionRepository.findByProductId(productId);
        CookingInstruction instruction = existingOpt.orElse(new CookingInstruction());

        instruction.setProduct(product);
        instruction.setContent(request.getContent());
        instruction.setUpdatedBy(user);

        return mapToResponse(cookingInstructionRepository.save(instruction));
    }

    private CookingInstructionResponse mapToResponse(CookingInstruction instruction) {
        CookingInstructionResponse response = new CookingInstructionResponse();
        response.setId(instruction.getId());
        response.setProductId(instruction.getProduct().getId());
        response.setContent(instruction.getContent());
        response.setUpdatedById(instruction.getUpdatedBy().getId());
        response.setUpdatedByName(instruction.getUpdatedBy().getUsername());
        response.setUpdatedAt(instruction.getUpdatedAt());
        return response;
    }
}
