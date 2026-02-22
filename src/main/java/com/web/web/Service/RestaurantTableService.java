package com.web.web.Service;

import com.web.web.Dto.RestaurantTableDTO;
import com.web.web.Entity.RestaurantTable;
import com.web.web.Entity.TableArea;
import com.web.web.Repository.RestaurantTableRepository;
import com.web.web.Repository.TableAreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantTableService {

    private final RestaurantTableRepository tableRepository;
    private final TableAreaRepository areaRepository;

    @Autowired
    public RestaurantTableService(RestaurantTableRepository tableRepository,
            TableAreaRepository areaRepository) {
        this.tableRepository = tableRepository;
        this.areaRepository = areaRepository;
    }

    private RestaurantTableDTO toDTO(RestaurantTable t) {
        return new RestaurantTableDTO(
                t.getId(),
                t.getName(),
                t.getStatus().name(),
                t.getArea().getId(),
                t.getArea().getName());
    }

    public List<RestaurantTableDTO> getAllTables() {
        return tableRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<RestaurantTableDTO> getTablesByArea(Long areaId) {
        return tableRepository.findByAreaId(areaId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public RestaurantTableDTO getTableById(Long id) {
        RestaurantTable t = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + id));
        return toDTO(t);
    }

    public RestaurantTableDTO createTable(RestaurantTableDTO dto) {
        TableArea area = areaRepository.findById(dto.getAreaId())
                .orElseThrow(() -> new IllegalArgumentException("Khu vực không tồn tại: " + dto.getAreaId()));
        RestaurantTable table = new RestaurantTable();
        table.setName(dto.getName());
        table.setStatus(RestaurantTable.TableStatus.EMPTY);
        table.setArea(area);
        return toDTO(tableRepository.save(table));
    }

    public RestaurantTableDTO updateTable(Long id, RestaurantTableDTO dto) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + id));
        if (dto.getName() != null)
            table.setName(dto.getName());
        if (dto.getAreaId() != null) {
            TableArea area = areaRepository.findById(dto.getAreaId())
                    .orElseThrow(() -> new IllegalArgumentException("Khu vực không tồn tại: " + dto.getAreaId()));
            table.setArea(area);
        }
        return toDTO(tableRepository.save(table));
    }

    public RestaurantTableDTO updateTableStatus(Long id, String statusStr) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + id));
        RestaurantTable.TableStatus status;
        try {
            status = RestaurantTable.TableStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + statusStr);
        }
        table.setStatus(status);
        return toDTO(tableRepository.save(table));
    }

    public void deleteTable(Long id) {
        if (!tableRepository.existsById(id)) {
            throw new RuntimeException("Bàn không tồn tại với ID: " + id);
        }
        tableRepository.deleteById(id);
    }
}
