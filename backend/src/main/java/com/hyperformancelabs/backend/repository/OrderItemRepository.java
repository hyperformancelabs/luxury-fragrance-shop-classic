package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    // Tìm tất cả order item của một order
    List<OrderItem> findByOrder_OrderId(Integer orderId);
}
