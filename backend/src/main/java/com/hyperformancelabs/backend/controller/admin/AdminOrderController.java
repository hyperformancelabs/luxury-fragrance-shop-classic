package com.hyperformancelabs.backend.controller.admin;

import com.hyperformancelabs.backend.dto.AdminOrderDisplayDTO;
import com.hyperformancelabs.backend.dto.CustomerDTO;
import com.hyperformancelabs.backend.dto.OrderItemDisplayDTO;
import com.hyperformancelabs.backend.dto.OrderDTO;
import com.hyperformancelabs.backend.service.CustomerService;
import com.hyperformancelabs.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String range, // thêm param range
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        // Xử lý khoảng thời gian theo range nếu có
        LocalDate today = LocalDate.now();
        if ("today".equals(range)) {
            startDate = today;
            endDate = today;
        } else if ("yesterday".equals(range)) {
            startDate = today.minusDays(1);
            endDate = today.minusDays(1);
        } else if ("week".equals(range)) {
            startDate = today.minusDays(7);
            endDate = today;
        } else if ("month".equals(range)) {
            startDate = today.minusDays(30);
            endDate = today;
        }

        // Lấy thống kê
        Map<String, String> stats = orderService.getOrderStatistics();

        // Phân trang
        Pageable pageable = PageRequest.of(page, 10);

        // Truy vấn đơn hàng
        Page<AdminOrderDisplayDTO> ordersPage = orderService.findOrdersByDateAndStatus(
               keyword, status, startDate, endDate, pageable
        );

        // Build queryParams để giữ lại filter khi phân trang
        StringBuilder queryParams = new StringBuilder();
        if (keyword != null && !keyword.isBlank()) queryParams.append("&keyword=").append(keyword);
        if (status != null && !status.isBlank()) queryParams.append("&status=").append(status);
        if (range != null && !range.isBlank()) queryParams.append("&range=").append(range);
        if (startDate != null) queryParams.append("&startDate=").append(startDate);
        if (endDate != null) queryParams.append("&endDate=").append(endDate);

        // Truyền sang view
        model.addAttribute("queryParams", queryParams.toString());
        model.addAttribute("stats", stats);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("status", status);
        model.addAttribute("range", range);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("orders", ordersPage.getContent());

        return "admin/orders/list";
    }



    @GetMapping("/{id}")
    public String viewOrder(@PathVariable("id") Integer id, Model model) {

        // Order info
        AdminOrderDisplayDTO orders = orderService.findOrderDetailById(id);

        // Order items info
        List<OrderItemDisplayDTO> orderItems = orderService.getOrderItemsByOrderId(id);
        OrderDTO order = orderService.findOrderById(id);
        BigDecimal subtotal = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Order timeline
        List<String> statusSteps = List.of("pending", "confirmed", "processing", "shipping", "delivered");


        model.addAttribute("order", orders);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("shippingFee", order.getShippingFee());
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("statusSteps", statusSteps);

        System.out.println("orderItems: "+orderItems);
        
        return "admin/orders/detail";
    }

    @Transactional
    @PostMapping("/update-status")
    public String updateOrderStatus(@RequestParam Integer orderId,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        orderService.updateOrderStatus(orderId, status); // cập nhật trạng thái trong DB
        redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công");
        return "redirect:/admin/orders/" + orderId;
    }

    @GetMapping("/edit/{orderId}")
    public String editOrderForm(@PathVariable Integer orderId, Model model) {
        OrderDTO order = orderService.findOrderById(orderId);

        CustomerDTO customer = customerService.getCustomerById(order.getCustomerId());

        model.addAttribute("order", order);
        model.addAttribute("customer", customer);
        return "admin/orders/edit";
    }

    @PostMapping("/update-info")
    public String updateOrderInfo(@RequestParam Integer orderId,
                                  @RequestParam Integer customerId,
                                  @RequestParam String name,
                                  @RequestParam String email,
                                  @RequestParam String phoneNumber,
                                  @RequestParam String shippingAddress,
                                  RedirectAttributes redirectAttributes) {
        orderService.updateShippingAddress(orderId, shippingAddress);
        customerService.updateCustomerInfo(customerId.toString(), name, email, phoneNumber);
        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công");
        return "redirect:/admin/orders/" + orderId;
    }
}
