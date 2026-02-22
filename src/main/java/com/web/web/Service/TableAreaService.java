package com.web.web.Service;

import com.web.web.Dto.TableAreaDTO;
import com.web.web.Entity.TableArea;
import com.web.web.Repository.RestaurantTableRepository;
import com.web.web.Repository.TableAreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableAreaService {

    private final TableAreaRepository tableAreaRepository;
    private final RestaurantTableRepository tableRepository;

    @Autowired
    public TableAreaService(TableAreaRepository tableAreaRepository,
            RestaurantTableRepository tableRepository) {
        this.tableAreaRepository = tableAreaRepository;
        this.tableRepository = tableRepository;
    }

    private TableAreaDTO toDTO(TableArea a) {
        long count = tableRepository.countByAreaId(a.getId());
        return new TableAreaDTO(a.getId(), a.getName(), (int) count);
    }

    public List<TableAreaDTO> getAllAreas() {
        return tableAreaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TableAreaDTO getAreaById(Long id) {
        TableArea area = tableAreaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khu vực không tồn tại với ID: " + id));
        return toDTO(area);
    }

    public TableAreaDTO createArea(TableAreaDTO dto) {
        if (tableAreaRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Tên khu vực đã tồn tại: " + dto.getName());
        }
        TableArea area = new TableArea(dto.getName());
        area = tableAreaRepository.save(area);
        return new TableAreaDTO(area.getId(), area.getName(), 0);
    }

    public TableAreaDTO updateArea(Long id, TableAreaDTO dto) {
        TableArea area = tableAreaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khu vực không tồn tại với ID: " + id));
        area.setName(dto.getName());
        area = tableAreaRepository.save(area);
        return toDTO(area);
    }

    public void deleteArea(Long id) {
        if (!tableAreaRepository.existsById(id)) {
            throw new RuntimeException("Khu vực không tồn tại với ID: " + id);
        }
        tableAreaRepository.deleteById(id);
    }
}
