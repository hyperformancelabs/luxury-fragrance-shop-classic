package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.OrderItemDTO;

import java.util.List;

public interface OrderItemService {

    // Lưu order item
    void save(OrderItemDTO orderItem);

    // Lấy tất cả order item của một order
    List<OrderItemDTO> findByOrder_OrderId(Integer orderId);
}
