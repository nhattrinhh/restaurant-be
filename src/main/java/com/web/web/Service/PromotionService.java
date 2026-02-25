package com.web.web.Service;

import com.web.web.Dto.PromotionDto;

import java.util.List;

public interface PromotionService {
    List<PromotionDto> getAll();

    PromotionDto create(PromotionDto dto);

    PromotionDto update(Long id, PromotionDto dto);

    void delete(Long id);

    PromotionDto toggleStatus(Long id);
}
