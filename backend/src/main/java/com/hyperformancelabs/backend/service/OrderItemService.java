package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.common.response.OrderItemDTO;
import com.hyperformancelabs.backend.model.OrderItem;

import java.util.List;

public interface OrderItemService {

    // Lưu order item
    void save(OrderItemDTO orderItem);

    // Lấy tất cả order item của một order dạng DTO
    List<OrderItemDTO> findByOrder_OrderId(Integer orderId);
    
    // Lấy tất cả order item của một order dạng Entity
    List<OrderItem> findOrderItemsByOrderId(Integer orderId);
}
