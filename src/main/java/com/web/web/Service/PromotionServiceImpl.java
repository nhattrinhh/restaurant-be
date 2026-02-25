package com.web.web.Service;

import com.web.web.Dto.PromotionDto;
import com.web.web.Entity.Promotion;
import com.web.web.Entity.Promotion.PromotionStatus;
import com.web.web.Entity.Promotion.PromotionType;
import com.web.web.Repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionServiceImpl implements PromotionService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private PromotionRepository repo;

    @Override
    public List<PromotionDto> getAll() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public PromotionDto create(PromotionDto dto) {
        if (repo.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Mã voucher '" + dto.getCode() + "' đã tồn tại");
        }
        Promotion p = toEntity(new Promotion(), dto);
        return toDto(repo.save(p));
    }

    @Override
    public PromotionDto update(Long id, PromotionDto dto) {
        Promotion p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi với ID: " + id));
        // If code changed, check uniqueness
        if (!p.getCode().equals(dto.getCode()) && repo.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Mã voucher '" + dto.getCode() + "' đã tồn tại");
        }
        toEntity(p, dto);
        return toDto(repo.save(p));
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Không tìm thấy khuyến mãi với ID: " + id);
        }
        repo.deleteById(id);
    }

    @Override
    public PromotionDto toggleStatus(Long id) {
        Promotion p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi với ID: " + id));
        if (p.getStatus() == PromotionStatus.ACTIVE) {
            p.setStatus(PromotionStatus.PAUSED);
        } else if (p.getStatus() == PromotionStatus.PAUSED) {
            p.setStatus(PromotionStatus.ACTIVE);
        } else {
            throw new IllegalStateException("Không thể toggle khuyến mãi đã EXPIRED");
        }
        return toDto(repo.save(p));
    }

    // -------- Mapping helpers --------

    private PromotionDto toDto(Promotion p) {
        PromotionDto dto = new PromotionDto();
        dto.setId(p.getId());
        dto.setCode(p.getCode());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setType(p.getType() != null ? p.getType().name() : null);
        dto.setValue(p.getValue());
        dto.setMinOrderValue(p.getMinOrderValue());
        dto.setUsageLimit(p.getUsageLimit());
        dto.setUsedCount(p.getUsedCount());
        dto.setStartDate(p.getStartDate() != null ? p.getStartDate().format(FMT) : null);
        dto.setEndDate(p.getEndDate() != null ? p.getEndDate().format(FMT) : null);
        dto.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        dto.setCreatedAt(p.getCreatedAt() != null ? p.getCreatedAt().format(FMT) : null);
        dto.setUpdatedAt(p.getUpdatedAt() != null ? p.getUpdatedAt().format(FMT) : null);
        return dto;
    }

    private Promotion toEntity(Promotion p, PromotionDto dto) {
        p.setCode(dto.getCode() != null ? dto.getCode().trim().toUpperCase() : null);
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setType(dto.getType() != null ? PromotionType.valueOf(dto.getType()) : PromotionType.PERCENT);
        p.setValue(dto.getValue() != null ? dto.getValue() : BigDecimal.ZERO);
        p.setMinOrderValue(dto.getMinOrderValue() != null ? dto.getMinOrderValue() : BigDecimal.ZERO);
        p.setUsageLimit(dto.getUsageLimit() != null ? dto.getUsageLimit() : 100);
        if (p.getUsedCount() == null)
            p.setUsedCount(0);
        p.setStartDate(dto.getStartDate() != null ? LocalDateTime.parse(dto.getStartDate(), FMT) : null);
        p.setEndDate(dto.getEndDate() != null ? LocalDateTime.parse(dto.getEndDate(), FMT) : null);
        p.setStatus(dto.getStatus() != null ? PromotionStatus.valueOf(dto.getStatus()) : PromotionStatus.ACTIVE);
        return p;
    }
}
