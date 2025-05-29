package com.hyperformancelabs.backend.controller.admin;

import com.hyperformancelabs.backend.dto.LowStockProductDTO;
import com.hyperformancelabs.backend.dto.RecentOrderDTO;
import com.hyperformancelabs.backend.dto.TopSellingProductDTO;
import com.hyperformancelabs.backend.service.CustomerService;
import com.hyperformancelabs.backend.service.InventoryService;
import com.hyperformancelabs.backend.service.OrderService;
import com.hyperformancelabs.backend.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;
    
    @Autowired
    private InventoryService inventoryService;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model,
                            @RequestParam(value = "range", required = false, defaultValue = "today") String range,
                            @RequestParam(value = "start", required = false) String startDateStr,
                            @RequestParam(value = "end", required = false) String endDateStr,
                            HttpSession session) {

        // Lấy role từ session
        List<String> roles = (List<String>) session.getAttribute("ROLES");

        if (roles == null) {
            return "redirect:/admin/login";
        }

        // ✅ Điều hướng theo phân quyền
        if (roles.contains("System Admin")) {
            // Tiếp tục xử lý dashboard như cũ
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            Map<String, Object> revenueData = new HashMap<>();
            Map<String, Object> totalOrdersData = new HashMap<>();
            Map<String, Object> newCustomerData = new HashMap<>();

            if (range.equals("custom") && startDateStr != null && endDateStr != null) {
                LocalDate start = LocalDate.parse(startDateStr);
                LocalDate end = LocalDate.parse(endDateStr);
                revenueData.put("custom", orderService.getTotalRevenueBetweenDates(start, end));
                totalOrdersData.put("custom", orderService.countOrdersBetweenDates(start, end));
                newCustomerData.put("custom", customerService.countNewCustomersBetweenDates(start, end));
                model.addAttribute("startDate", startDateStr);
                model.addAttribute("endDate", endDateStr);
            } else {
                revenueData.put("today", orderService.getTotalRevenueToday());
                revenueData.put("thisMonth", orderService.getTotalRevenueCurrentMonth());
                revenueData.put("thisYear", orderService.getTotalRevenueCurrentYear());

                totalOrdersData.put("today", orderService.countOrdersToday());
                totalOrdersData.put("thisMonth", orderService.countOrdersThisMonth());
                totalOrdersData.put("thisYear", orderService.countOrdersThisYear());

                newCustomerData.put("today", customerService.countNewCustomersToday());
                newCustomerData.put("thisMonth", customerService.countNewCustomersThisMonth());
                newCustomerData.put("thisYear", customerService.countNewCustomersThisYear());
            }

            Map<String, BigDecimal> salesMonthlyData = orderService.getMonthlyRevenueTillNowOfCurrentYear();
            Map<String, BigDecimal> salesYearlyData = orderService.getRevenueByYearRange(2023, 2025);

            List<TopSellingProductDTO> topProducts = productService.getTopSellingProducts(null, 5);
            List<LowStockProductDTO> lowStockProducts = inventoryService.getLowStockProducts(5);
            List<RecentOrderDTO> recentOrders = orderService.findTop3ByOrderByOrderDateDesc();

            model.addAttribute("roles", roles);
            model.addAttribute("activeMenu", "dashboard");
            model.addAttribute("today", today.format(formatter));
            model.addAttribute("revenueData", revenueData);
            model.addAttribute("totalOrdersData", totalOrdersData);
            model.addAttribute("newCustomerData", newCustomerData);
            model.addAttribute("salesMonthlyData", salesMonthlyData);
            model.addAttribute("salesYearlyData", salesYearlyData);
            model.addAttribute("topProducts", topProducts);
            model.addAttribute("lowStockProducts", lowStockProducts);
            model.addAttribute("recentOrders", recentOrders);

            return "admin/dashboard";
        }

        // ✅ Nếu không phải system admin → điều hướng theo quyền đầu tiên
        if (roles.contains("Order Staff")) {
            return "redirect:/admin/dashboard";
        }

        if (roles.contains("Material Staff")) {
            return "redirect:/admin/products";
        }

        // ❌ Không có quyền phù hợp
        return "redirect:/admin/access-denied";
    }
}
