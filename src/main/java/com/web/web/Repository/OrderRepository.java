package com.web.web.Repository;

import com.web.web.Entity.Order;
import com.web.web.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}
