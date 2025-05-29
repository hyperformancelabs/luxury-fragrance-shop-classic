package com.hyperformancelabs.backend.controller.admin;

import com.hyperformancelabs.backend.dto.CustomerDTO;
import com.hyperformancelabs.backend.dto.OrderDTO;
import com.hyperformancelabs.backend.dto.ProductPurchaseInfoDTO;
import com.hyperformancelabs.backend.service.CustomerService;
import com.hyperformancelabs.backend.service.OrderService;
import org.flywaydb.core.internal.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10);

        // Xử lý sortDir mặc định
        if (sortBy != null && sortDir == null) {
            sortBy = "name";
            sortDir = "ASC";
        }

        Page<CustomerDTO> customersPage = customerService.findCustomersWithOptionalStatusAndSort(
                keyword,
                status,
                sortBy,
                sortDir,
                pageable
        );

        // Xây dựng chuỗi query giữ lại filter
        StringBuilder queryParams = new StringBuilder();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryParams.append("&keyword=").append(URLEncoder.encode(keyword, StandardCharsets.UTF_8));
        }
        if (status != null && !status.trim().isEmpty()) {
            queryParams.append("&status=").append(status);
        }
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            queryParams.append("&sortBy=").append(sortBy);
        }
        if (sortDir != null && !sortDir.trim().isEmpty()) {
            queryParams.append("&sortDir=").append(sortDir);
        }

        model.addAttribute("customers", customersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customersPage.getTotalPages());
        model.addAttribute("keyword", keyword); // <-- CHỈNH LẠI Ở ĐÂY
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("status", status);
        model.addAttribute("queryParams", queryParams.toString());

        return "admin/customers/list";
    }

    @GetMapping("/{id}")
    public String viewCustomer(
            @PathVariable("id") Integer id,
            @RequestParam(value = "chartType", defaultValue = "month") String chartType,
            @RequestParam(value = "selectedYear", required = false) Integer selectedYear,
            Model model) {

        // Get customer info
        CustomerDTO customerInfo = customerService.getCustomerById(id);

        // Get order history
        List<OrderDTO> orderHistory = orderService.findOrdersByCustomerId(id);

        // Get purchased products
        List<ProductPurchaseInfoDTO> purchasedProducts = orderService.findPurchasedProductsByCustomerId(id);

        // Total orders
        int totalOrders = orderHistory.size();

        // Dữ liệu theo tháng của selectedYear (nếu không có thì lấy năm hiện tại)
        int yearForMonth = selectedYear != null ? selectedYear : LocalDateTime.now().getYear();
        List<Map<String, Object>> monthlySpending = new ArrayList<>();
        int currentMonth = LocalDateTime.now().getMonthValue();
        for (int month = 1; month <= currentMonth; month++) {
            BigDecimal totalSpent = orderService.getTotalSpentByCustomerInMonthAndYear(id, month, yearForMonth);
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month);
            monthData.put("totalSpent", totalSpent);
            monthlySpending.add(monthData);
        }

        // Dữ liệu theo năm (2023 đến hiện tại)
        List<Map<String, Object>> yearlySpending = new ArrayList<>();
        int currentYear = LocalDateTime.now().getYear();
        for (int year = 2023; year <= currentYear; year++) {
            BigDecimal totalSpent = orderService.getTotalSpentByCustomerInYear(id, year);
            Map<String, Object> yearData = new HashMap<>();
            yearData.put("year", year);
            yearData.put("totalSpent", totalSpent);
            yearlySpending.add(yearData);
        }

        // Truyền model
        model.addAttribute("customer", customerInfo);
        model.addAttribute("orderHistory", orderHistory);
        model.addAttribute("purchasedProducts", purchasedProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("chartType", chartType);
        model.addAttribute("selectedYear", yearForMonth);
        model.addAttribute("monthlySpending", monthlySpending);
        model.addAttribute("yearlySpending", yearlySpending);

        System.out.println("total purchased products: " + purchasedProducts.size());

        return "admin/customers/detail";
    }

    @PostMapping("/create")
    public String createCustomer(@ModelAttribute CustomerDTO customer,
                                 RedirectAttributes redirectAttributes) {
        try {
            customerService.addCustomer(customer);
            redirectAttributes.addFlashAttribute("success", "Tạo khách hàng mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Tạo khách hàng thất bại: " + e.getMessage());
        }
        return "redirect:/admin/customers";
    }

    @PostMapping("/update")
    @Transactional
    public String updateCustomer(@ModelAttribute CustomerDTO customer,
                                 RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Updating customer: " + customer);
            customerService.updateCustomer(customer);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin khách hàng thành công!");
        } catch (Exception e) {
            // Lấy nguyên nhân gốc
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            System.err.println("Lỗi khi cập nhật khách hàng: " + (rootCause != null ? rootCause.getMessage() : e.getMessage()));
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật thất bại: " + (rootCause != null ? rootCause.getMessage() : "Lỗi không xác định"));
        }

        return "redirect:/admin/customers/" + customer.getCustomerId(); // điều hướng đúng path
    }

    @Transactional
    @PostMapping("/delete")
    public String deleteCustomer(@RequestParam("customerId") Integer customerId,
                                 RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(customerId);
            redirectAttributes.addFlashAttribute("success", "Xóa khách hàng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa khách hàng: " + e.getMessage());
            throw new RuntimeException("Xóa khách hàng thất bại", e);
        }

        return "redirect:/admin/customers";
    }
}
