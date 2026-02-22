package com.web.web.Dto;

import lombok.Data;

@Data
public class RestaurantTableDTO {
    private Long id;
    private String name;
    private String status;
    private Long areaId;
    private String areaName;

    public RestaurantTableDTO() {
    }

    public RestaurantTableDTO(Long id, String name, String status, Long areaId, String areaName) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.areaId = areaId;
        this.areaName = areaName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}
