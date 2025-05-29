package com.hyperformancelabs.backend.controller;

import com.hyperformancelabs.backend.dto.*;
import com.hyperformancelabs.backend.model.OrderItem;
import com.hyperformancelabs.backend.service.*;
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
    public String orderSuccess(@RequestParam("orderId") Integer orderId, Model model) {
        if (orderId == null) {
            return "redirect:/";
        }

        // Lấy đơn hàng
        OrderDTO order = orderService.findOrderById(orderId);
        if (order == null) {
            return "redirect:/";
        }

        CustomerDTO customer = customerService.getCustomerById(order.getCustomerId());

        // Lấy sản phẩm đã đặt
        List<OrderItemDTO> orderItems = orderItemService.findByOrder_OrderId(orderId);

        // Chuyển đổi sang danh sách DTO để hiển thị
        List<OrderSuccessItemDTO> itemDTOs = orderItems.stream().map(item -> {
            ProductVariantDTO variant = productVariantService.getProductVariantById(item.getProductVariantId());
            ProductDTO product = productService.getProductById(variant.getProductId());

            return new OrderSuccessItemDTO(
                    product.getProductName(),
                    variant.getVolume() + "ml",
                    item.getQuantity(),
                    item.getUnitPrice(),
                    product.getImageUrl()
            );
        }).toList();

        // Tính tổng giá
        BigDecimal subtotal = orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(1_000_000)) > 0 ? BigDecimal.ZERO : BigDecimal.valueOf(30000);
        BigDecimal total = subtotal.add(shipping);

        // Tạo DTO hiển thị
        OrderSuccessDTO dto = new OrderSuccessDTO(
                order.getOrderId(),
                order.getOrderDate(),
                order.getShippingOption(),
                order.getShippingAddress(),
                customer.getName(),      // Phải cập nhật trong OrderDTO nếu chưa có
                customer.getPhoneNumber(),     // Phải cập nhật trong OrderDTO nếu chưa có
                itemDTOs,
                subtotal,
                shipping,
                total
        );

        model.addAttribute("order", dto);
        model.addAttribute("orderItems", dto.getItems());

        model.addAttribute("subtotal", dto.getSubtotal());
        model.addAttribute("shipping", dto.getShippingFee());
        model.addAttribute("total", dto.getTotal());
        return "order/order-success";
    }


    private Map<String, Object> createOrderItem(int id, String name, int quantity, int price, String imageUrl, String size) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("name", name);
        item.put("quantity", quantity);
        item.put("price", price);
        item.put("imageUrl", imageUrl);
        item.put("size", size);
        return item;
    }
}
