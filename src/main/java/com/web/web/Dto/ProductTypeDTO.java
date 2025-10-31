package com.web.web.Dto;

import lombok.Data;

@Data
public class ProductTypeDTO {
    private Long id;
    private String name;

    public ProductTypeDTO() {}

    public ProductTypeDTO(Long id, String name) {
        this.id = id;
        this.name = name;
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
}