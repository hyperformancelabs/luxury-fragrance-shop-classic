package com.hyperformancelabs.backend.dto.user.response;

// Ensure OrderSuccessItemDTO is also in this package or imported correctly
import com.hyperformancelabs.backend.dto.user.response.OrderSuccessItemDTO; 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSuccessDTO {

    private Integer orderId;
    private LocalDateTime orderDate;
    private String paymentMethod;
    private String orderStatus;

    private String shippingAddress;
    private String customerName;
    private String customerPhone;

    private List<OrderSuccessItemDTO> items;

    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal total;
    
    // Constructor without orderStatus for backward compatibility
    public OrderSuccessDTO(Integer orderId, LocalDateTime orderDate, String paymentMethod,
                          String shippingAddress, String customerName, String customerPhone,
                          List<OrderSuccessItemDTO> items, BigDecimal subtotal,
                          BigDecimal shippingFee, BigDecimal total) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.paymentMethod = paymentMethod;
        this.shippingAddress = shippingAddress;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.items = items;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.total = total;
        this.orderStatus = "pending"; // Default value
    }
} 