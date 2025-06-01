package com.hyperformancelabs.backend.controller.user;

import com.hyperformancelabs.backend.dto.common.response.OrderDTO;
import com.hyperformancelabs.backend.dto.common.response.CustomerDTO;
import com.hyperformancelabs.backend.dto.common.response.OrderItemDTO;
import com.hyperformancelabs.backend.dto.user.response.OrderSuccessItemDTO;
import com.hyperformancelabs.backend.dto.common.response.ProductVariantDTO;
import com.hyperformancelabs.backend.dto.common.response.ProductDTO;
import com.hyperformancelabs.backend.dto.user.response.OrderSuccessDTO;

import com.hyperformancelabs.backend.model.OrderItem;
import com.hyperformancelabs.backend.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping()
public class OrderSuccessController {

    private static final Logger logger = LoggerFactory.getLogger(OrderSuccessController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/order-success")
    public String orderSuccess(@RequestParam("orderId") Integer orderId, Model model, HttpServletRequest request) {
        logger.info("Loading order success page for orderId: {}", orderId);
        
        if (orderId == null) {
            logger.warn("Order ID is null, redirecting to home");
            return "redirect:/";
        }

        try {
            // Cập nhật số lượng giỏ hàng trong session thành 0 để hiển thị chính xác
            updateCartItemCountInSession(request);
            
            // Lấy đơn hàng
            OrderDTO order = orderService.findOrderById(orderId);
            if (order == null) {
                logger.warn("Order not found for ID: {}", orderId);
                return "redirect:/";
            }
            logger.debug("Found order: {}", order.getOrderId());

            CustomerDTO customer = customerService.getCustomerById(order.getCustomerId());
            if (customer == null) {
                logger.error("Customer not found for ID: {}", order.getCustomerId());
                throw new RuntimeException("Customer not found for order");
            }
            logger.debug("Found customer: {}", customer.getName());

            // Lấy sản phẩm đã đặt
            List<OrderItemDTO> orderItems = orderItemService.findByOrder_OrderId(orderId);
            logger.debug("Found {} order items", orderItems.size());

            // Chuyển đổi sang danh sách DTO để hiển thị
            List<OrderSuccessItemDTO> itemDTOs = new ArrayList<>();
            
            for (OrderItemDTO item : orderItems) {
                try {
                    ProductVariantDTO variant = productVariantService.getProductVariantById(item.getProductVariantId());
                    if (variant == null) {
                        logger.warn("Product variant not found for ID: {}", item.getProductVariantId());
                        continue;
                    }
                    
                    ProductDTO product = productService.getProductById(variant.getProductId());
                    if (product == null) {
                        logger.warn("Product not found for ID: {}", variant.getProductId());
                        continue;
                    }
                    
                    itemDTOs.add(new OrderSuccessItemDTO(
                            product.getProductName(),
                            variant.getVolume() + "ml",
                            item.getQuantity(),
                            item.getUnitPrice(),
                            product.getImageUrl()
                    ));
                } catch (Exception e) {
                    logger.error("Error processing order item: {}", e.getMessage(), e);
                }
            }
            
            logger.debug("Converted {} order items to DTOs", itemDTOs.size());

            // Tính tổng giá
            BigDecimal subtotal = orderItems.stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(1_000_000)) > 0 ? BigDecimal.ZERO : BigDecimal.valueOf(30000);
            BigDecimal total = subtotal.add(shipping);
            logger.debug("Calculated totals: subtotal={}, shipping={}, total={}", subtotal, shipping, total);

            // Tạo DTO hiển thị
            OrderSuccessDTO dto = new OrderSuccessDTO(
                    order.getOrderId(),
                    order.getOrderDate(),
                    order.getShippingOption(),
                    order.getShippingAddress(),
                    customer.getName(),
                    customer.getPhoneNumber(),
                    itemDTOs,
                    subtotal,
                    shipping,
                    total
            );
            
            // Thêm trạng thái đơn hàng
            dto.setOrderStatus(order.getOrderStatus());

            model.addAttribute("order", dto);
            model.addAttribute("orderItems", dto.getItems());

            model.addAttribute("subtotal", dto.getSubtotal());
            model.addAttribute("shipping", dto.getShippingFee());
            model.addAttribute("total", dto.getTotal());
            
            logger.info("Successfully prepared order success page for orderId: {}", orderId);
            return "user/order/order-success";
        } catch (Exception e) {
            logger.error("Error loading order success page for orderId {}: {}", orderId, e.getMessage(), e);
            model.addAttribute("errorMessage", "Không thể tải thông tin đơn hàng. Lỗi: " + e.getMessage());
            return "error/error";
        }
    }

    private void updateCartItemCountInSession(HttpServletRequest request) {
        try {
            request.getSession().setAttribute("cartItemCount", 0);
            logger.info("Reset cart item count in session to 0");
        } catch (Exception e) {
            logger.error("Error updating cart item count in session: {}", e.getMessage());
        }
    }
} 