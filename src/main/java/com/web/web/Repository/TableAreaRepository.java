package com.web.web.Repository;

import com.web.web.Entity.TableArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableAreaRepository extends JpaRepository<TableArea, Long> {
    boolean existsByName(String name);
}
