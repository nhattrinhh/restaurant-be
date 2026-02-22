package com.web.web.Dto;

import lombok.Data;

@Data
public class TableAreaDTO {
    private Long id;
    private String name;
    private int tableCount;

    public TableAreaDTO() {
    }

    public TableAreaDTO(Long id, String name, int tableCount) {
        this.id = id;
        this.name = name;
        this.tableCount = tableCount;
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

    public int getTableCount() {
        return tableCount;
    }

    public void setTableCount(int tableCount) {
        this.tableCount = tableCount;
    }
}
