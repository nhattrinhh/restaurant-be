package com.web.web.Repository;

import com.web.web.Entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findByAreaId(Long areaId);

    List<RestaurantTable> findByStatus(RestaurantTable.TableStatus status);

    long countByAreaId(Long areaId);
}
