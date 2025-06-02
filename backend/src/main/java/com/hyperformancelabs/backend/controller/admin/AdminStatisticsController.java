package com.hyperformancelabs.backend.controller.admin;

import com.hyperformancelabs.backend.dto.admin.response.TopSellingDisplayDTO;
import com.hyperformancelabs.backend.service.CustomerService;
import com.hyperformancelabs.backend.service.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/statistics")
public class AdminStatisticsController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public String statistics(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {

        LocalDate today = LocalDate.now();

        if (startDate == null) {
            startDate = today.withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = today;
        }

        // Current period
        BigDecimal revenue = orderService.getTotalRevenueBetweenDates(startDate, endDate);
        long totalOrders = orderService.countOrdersBetweenDates(startDate, endDate);
        long newCustomers = customerService.countNewCustomersBetweenDates(startDate, endDate);

        // Previous period
        long days = startDate.until(endDate).getDays() + 1;
        LocalDate prevStart = startDate.minusDays(days);
        LocalDate prevEnd = endDate.minusDays(days);

        BigDecimal revenuePrev = orderService.getTotalRevenueBetweenDates(prevStart, prevEnd);
        long totalOrdersPrev = orderService.countOrdersBetweenDates(prevStart, prevEnd);
        long newCustomersPrev = customerService.countNewCustomersBetweenDates(prevStart, prevEnd);

        // Percentage change
        double revenueChange = calculateChangePercent(revenuePrev, revenue);
        double orderChange = calculateChangePercent(totalOrdersPrev, totalOrders);
        double customerChange = calculateChangePercent(newCustomersPrev, newCustomers);

        // Sales Data
        Map<String, BigDecimal> salesDailyData = new HashMap<>();
        salesDailyData = orderService.getDailyRevenueInMonth(startDate.getMonthValue(), startDate.getYear());
        Map<String, BigDecimal> salesWeeklyData = new HashMap<>();
        salesWeeklyData = orderService.getWeeklyRevenueInMonth(startDate.getMonthValue(), startDate.getYear());
        Map<String, BigDecimal> salesMonthlyData = new HashMap<>();
        salesMonthlyData = orderService.getMonthlyRevenueTillNowOfCurrentYear();
        Map<String, BigDecimal> salesYearlyData = new HashMap<>();
        salesYearlyData = orderService.getRevenueByYearRange(2023, 2025);
        Map<String, BigDecimal> salesByGender = new HashMap<>();
        salesByGender = orderService.getRevenueBySuitableGender(startDate.getYear());
        Map<String, BigDecimal> salesByBrand = new HashMap<>();
        salesByBrand = orderService.getRevenueByBrand(startDate.getYear());
        Map<String, BigDecimal> salesByProduct = new HashMap<>();
        List<TopSellingDisplayDTO> topSellingProducts = orderService.findTop5SellingVariants();
        BigDecimal totalRevenue = topSellingProducts.stream()
                .map(TopSellingDisplayDTO::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        for (TopSellingDisplayDTO product : topSellingProducts) {
            BigDecimal productRevenue = product.getRevenue();
            BigDecimal percent = BigDecimal.ZERO;

            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                percent = productRevenue.multiply(BigDecimal.valueOf(100))
                        .divide(totalRevenue, 1, BigDecimal.ROUND_HALF_UP);
            }

            product.setPercentage(percent + "%"); // Gán vào DTO
        }


        // Format để hiển thị
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        model.addAttribute("startDate", displayFormatter.format(startDate));
        model.addAttribute("endDate", displayFormatter.format(endDate));
        model.addAttribute("startDateRaw", startDate);
        model.addAttribute("endDateRaw", endDate);

        model.addAttribute("revenue", revenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("newCustomers", newCustomers);

        model.addAttribute("revenueChange", revenueChange);
        model.addAttribute("revenueChangeStr", String.format("%.1f", revenueChange));
        model.addAttribute("orderChange", orderChange);
        model.addAttribute("orderChangeStr", String.format("%.1f", orderChange));
        model.addAttribute("customerChange", customerChange);
        model.addAttribute("customerChangeStr", String.format("%.1f", customerChange));

        model.addAttribute("salesDailyData", salesDailyData);       // Map<String, BigDecimal>  -> "2025-05-01": 12000000
        model.addAttribute("salesWeeklyData", salesWeeklyData);     // Map<String, BigDecimal>  -> "Tuần 1": 50000000
        model.addAttribute("salesMonthlyData", salesMonthlyData);   // Map<String, BigDecimal>  -> "T1": 12000000
        model.addAttribute("salesYearlyData", salesYearlyData);     // Map<String, BigDecimal>  -> "2023": 1_000_000_000
        model.addAttribute("salesByGender", salesByGender);         // Map<String, BigDecimal>  -> "Men": 12000000
        model.addAttribute("salesByBrand", salesByBrand);             // Map<String, BigDecimal>  -> "Chanel": 12000000
        model.addAttribute("topSellingProducts", topSellingProducts);


        System.out.println("Revenue: " + revenue);
        System.out.println("Revenue prev: " + revenuePrev);
        System.out.println("Total orders: " + totalOrders);
        System.out.println("Total orders prev: " + totalOrdersPrev);
        System.out.println("New customers: " + newCustomers);
        System.out.println("New customers prev: " + newCustomersPrev);
        System.out.println("Revenue change: " + revenueChange);
        System.out.println("Order change: " + orderChange);
        System.out.println("Customer change: " + customerChange);

        System.out.println("Start date: " + startDate);
        System.out.println("End date: " + endDate);
        System.out.println("Prev start date: " + prevStart);
        System.out.println("Prev end date: " + prevEnd);

        System.out.println("Sales daily data: " + salesDailyData);
        System.out.println("Sales weekly data: " + salesWeeklyData);
        System.out.println("Sales monthly data: " + salesMonthlyData);
        System.out.println("Sales yearly data: " + salesYearlyData);
        System.out.println("Sales by gender: " + salesByGender);
        System.out.println("Sales by brand: " + salesByBrand);

        return "admin/statistics";
    }

    private double calculateChangePercent(Number oldValue, Number newValue) {
        if (oldValue == null || oldValue.doubleValue() == 0) {
            return (newValue != null && newValue.doubleValue() > 0) ? 100.0 : 0.0;
        }
        if (newValue == null) {
            return -100.0;
        }
        return ((newValue.doubleValue() - oldValue.doubleValue()) / oldValue.doubleValue()) * 100.0;
    }

    @GetMapping("/export")
    public void exportRevenueReport(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                    @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                    HttpServletResponse response) throws IOException {
        // Lấy dữ liệu từ service
        Map<String, BigDecimal> salesData = orderService.getSalesDailyData(startDate, endDate);

        // Tạo workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Doanh thu");

        // Header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Ngày");
        header.createCell(1).setCellValue("Doanh thu (VND)");

        // Dữ liệu
        int rowIdx = 1;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entry : salesData.entrySet()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue().doubleValue());
            totalRevenue = totalRevenue.add(entry.getValue());
        }

        // Thêm hàng tổng
        Row totalRow = sheet.createRow(rowIdx);
        totalRow.createCell(0).setCellValue("Tổng doanh thu");
        totalRow.createCell(1).setCellValue(totalRevenue.doubleValue());

        // Tên file
        String fileName = "BaoCao_DoanhThu_" + startDate + "_den_" + endDate + ".xlsx";

        // Cấu hình response
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

        // Ghi file
        workbook.write(response.getOutputStream());
        workbook.close();
    }



}

