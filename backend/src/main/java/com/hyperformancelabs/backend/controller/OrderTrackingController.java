package com.hyperformancelabs.backend.controller;

import com.hyperformancelabs.backend.dto.OrderDTO;
import com.hyperformancelabs.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/track-order")
public class OrderTrackingController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/my-orders")
    public String redirectOrders(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/profile/orders";
        } else {
            return "redirect:/track-order";
        }
    }

    @GetMapping
    public String showTrackOrderForm() {
        return "order/track-order";
    }

    @GetMapping("/search")
    public String trackOrder(@RequestParam String phone, Model model) {
        List<OrderDTO> orders = orderService.findByPhone(phone);
        if (orders.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy đơn hàng.");
            return "order/track-order";
        } else {
            model.addAttribute("orders", orders);
            return "order/order-list";
        }
    }
}

