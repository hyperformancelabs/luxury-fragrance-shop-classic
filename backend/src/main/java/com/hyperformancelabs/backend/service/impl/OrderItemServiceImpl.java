package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.OrderItemDTO;
import com.hyperformancelabs.backend.model.OrderItem;
import com.hyperformancelabs.backend.repository.OrderItemRepository;
import com.hyperformancelabs.backend.repository.OrderRepository;
import com.hyperformancelabs.backend.repository.ProductVariantRepository;
import com.hyperformancelabs.backend.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void save(OrderItemDTO orderItem) {
        orderItemRepository.save(convertToOrderItem(orderItem));
    }

    @Override
    public List<OrderItemDTO> findByOrder_OrderId(Integer orderId) {
        return orderItemRepository.findByOrder_OrderId(orderId)
                .stream()
                .map(this::convertToOrderItemDTO)
                .collect(Collectors.toList());
    }

    private OrderItem convertToOrderItem(OrderItemDTO orderItem) {
        return new OrderItem(
                orderItem.getOrderItemId(),
                orderRepository.findByOrderId(orderItem.getOrderId()),
                productVariantRepository.findByProductVariantId(orderItem.getProductVariantId()),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getNote()
        );
    }

    private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
        return new OrderItemDTO(
                orderItem.getOrderItemId(),
                orderItem.getOrder().getOrderId(),
                orderItem.getProductVariant().getProductVariantId(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getNote()
        );
    }
}
