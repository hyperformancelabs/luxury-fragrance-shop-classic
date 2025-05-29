package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.*;
import com.hyperformancelabs.backend.model.Order;
import com.hyperformancelabs.backend.repository.OrderRepository;
import com.hyperformancelabs.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public OrderDTO findOrderById(Integer orderId) {
        Order order = orderRepository.findByOrderId(orderId);
        return order != null ? convertToOrderDTO(order) : null;
    }

    @Override
    public List<OrderDTO> findOrdersByCustomerId(Integer customerId) {
        return orderRepository.findByCustomer_CustomerIdOrderByOrderDateDesc(customerId)
                .stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> findByPhone(String phone) {
        return orderRepository.findByCustomer_PhoneNumber(phone)
                .stream()
                .map(this::convertToOrderDTO)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------- ADMIN -----------------------------------------------------

    // Tổng doanh thu ngày hôm nay
    @Override
    public BigDecimal getTotalRevenueToday() {
        return orderRepository.getTotalRevenueToday();
    }

    // Tổng doanh thu tuần hiện tại
    @Override
    public BigDecimal getTotalRevenueThisWeek() {
        return orderRepository.getTotalRevenueThisWeek();
    }

    // Tổng doanh thu tháng hiện tại
    @Override
    public BigDecimal getTotalRevenueCurrentMonth() {
        return orderRepository.getTotalRevenueCurrentMonth();
    }

    // Tổng doanh thu năm hiện tại
    @Override
    public BigDecimal getTotalRevenueCurrentYear() {
        return orderRepository.getTotalRevenueCurrentYear();
    }

    // Tổng doanh thu theo ngày
    @Override
    public BigDecimal getTotalRevenueByDate(LocalDate targetDate) {
        return orderRepository.getTotalRevenueByDate(targetDate);
    }

    // Tổng doanh thu theo tháng và năm
    @Override
    public BigDecimal getTotalRevenueByMonthAndYear(int month, int year) {
        return orderRepository.getTotalRevenueByMonthAndYear(month, year);
    }

    // Tổng doanh thu theo ngày tháng năm
    @Override
    public BigDecimal getTotalRevenueBetweenDates(LocalDate startDate, LocalDate endDate) {
        return orderRepository.getTotalRevenueBetweenDates(startDate, endDate);
    }

    // Tổng số đơn hàng ngày hôm nay
    @Override
    public Long countOrdersToday() {
        return orderRepository.countOrdersToday();
    }

    // Tổng số đơn hàng tháng hiện tại
    @Override
    public Long countOrdersThisMonth() {
        return orderRepository.countOrdersThisMonth();
    }

    // Tổng số đơn hàng năm hiện tại
    @Override
    public Long countOrdersThisYear() {
        return orderRepository.countOrdersThisYear();
    }

    // Tổng số đơn hàng theo tháng và năm
    @Override
    public Long countOrdersByMonthAndYear(int month, int year) {
        return orderRepository.countOrdersByMonthAndYear(month, year);
    }

    // Tổng số đơn hàng theo ngày tháng năm
    @Override
    public Long countOrdersBetweenDates(LocalDate startDate, LocalDate endDate) {
        return orderRepository.countOrdersBetweenDates(startDate, endDate);
    }

    // Lấy 3 đơn hàng mới nhất
    @Override
    public List<RecentOrderDTO> findTop3ByOrderByOrderDateDesc() {
        return orderRepository.findTop3ByOrderByOrderDateDesc()
                .stream()
                .map(this::convertToRecentOrderDTO)
                .collect(Collectors.toList());
    }

    // Lấy doanh thu theo tháng của năm hiện tại
    @Override
    public Map<String, BigDecimal> getMonthlyRevenueTillNowOfCurrentYear() {
        Map<String, BigDecimal> salesData = new LinkedHashMap<>();
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        for (int month = 1; month <= currentMonth; month++) {
            BigDecimal revenue = orderRepository.getTotalRevenueByMonthAndYear(month, currentYear);
            salesData.put("T" + month, revenue != null ? revenue : BigDecimal.ZERO);
        }

        return salesData;
    }

    // Lấy doanh thu theo năm
    @Override
    public Map<String, BigDecimal> getRevenueByYearRange(int startYear, int endYear) {
        Map<String, BigDecimal> yearlySales = new LinkedHashMap<>();

        for (int year = startYear; year <= endYear; year++) {
            BigDecimal total = BigDecimal.ZERO;

            for (int month = 1; month <= 12; month++) {
                BigDecimal revenue = orderRepository.getTotalRevenueByMonthAndYear(month, year);
                total = total.add(revenue != null ? revenue : BigDecimal.ZERO);
            }

            yearlySales.put(String.valueOf(year), total);
        }

        return yearlySales;
    }

    // Lấy doanh thu theo ngày trong tháng
    @Override
    public Map<String, BigDecimal> getDailyRevenueInMonth(int month, int year) {
        List<Object[]> results = orderRepository.getDailyRevenueInMonth(month, year);
        Map<String, BigDecimal> revenueMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Object[] row : results) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            BigDecimal revenue = (BigDecimal) row[1];
            revenueMap.put(date.format(formatter), revenue);
        }

        return revenueMap;
    }

    // Lấy doanh thu theo tuần trong tháng
    public Map<String, BigDecimal> getWeeklyRevenueInMonth(int month, int year) {
        Map<String, BigDecimal> weeklyRevenue = new LinkedHashMap<>();

        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        // Xác định ngày bắt đầu của tuần đầu tiên (Thứ 2 gần nhất trước hoặc là ngày đầu tháng)
        LocalDate current = startOfMonth;

        int weekIndex = 1;
        while (current.isBefore(endOfMonth) || current.isEqual(endOfMonth)) {
            // Tuần kết thúc vào Chủ nhật hoặc cuối tháng
            LocalDate endOfWeek = current.plusDays(6 - current.getDayOfWeek().getValue() % 7);
            if (endOfWeek.isAfter(endOfMonth)) {
                endOfWeek = endOfMonth;
            }

            // Gọi repository để lấy doanh thu giữa current và endOfWeek
            BigDecimal revenue = orderRepository.getTotalRevenueBetweenDates(current, endOfWeek);
            weeklyRevenue.put("Tuần " + weekIndex, revenue);

            // Bước sang tuần tiếp theo
            current = endOfWeek.plusDays(1);
            weekIndex++;
        }

        return weeklyRevenue;
    }

    // Lấy doanh thu theo loại sản phẩm
    @Override
    public Map<String, BigDecimal> getRevenueBySuitableGender(int year) {
        List<Object[]> results = orderRepository.getRevenueBySuitableGender(year);
        Map<String, BigDecimal> revenueMap = new LinkedHashMap<>();

        for (Object[] row : results) {
            String gender = (String) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            revenueMap.put(gender, revenue);
        }

        return revenueMap;
    }

    // Lấy doanh thu theo thương hiệu
    @Override
    public Map<String, BigDecimal> getRevenueByBrand(int year) {
        List<Object[]> results = orderRepository.getTopBrandsDominatingRevenue(year);
        Map<String, BigDecimal> revenueMap = new LinkedHashMap<>();

        for (Object[] row : results) {
            String brand = (String) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            revenueMap.put(brand, revenue);
        }

        return revenueMap;
    }

    @Override
    public List<TopSellingDisplayDTO> findTop5SellingVariants() {
        return orderRepository.findTop5SellingVariants()
                .stream()
                .map(this::convertToTopSellingDisplayDTO)
                .collect(Collectors.toList());
    }

    // Lấy đơn hàng theo trạng thái và khoảng thời gian
    @Override
    public Page<AdminOrderDisplayDTO> findOrdersByDateAndStatus(String keyword, String status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return orderRepository.findOrdersByDateAndStatus(keyword, status, startDate, endDate, pageable)
                .map(this::convertToAdminOrderDisplayDTO);
    }

    // Lấy số đơn thống kê
    @Override
    public Map<String, String> getOrderStatistics() {
        List<Long> results = orderRepository.getOrderStatistics();
        long total = results.get(0);
        long pending = results.get(1);
        long completed = results.get(2);

        Map<String, String> orderStats = new LinkedHashMap<>();
        orderStats.put("totalOrders", String.valueOf(total));
        orderStats.put("pendingOrders", String.valueOf(pending));
        orderStats.put("completedOrders", String.valueOf(completed));

        double rate = total > 0 ? (completed * 100.0 / total) : 0.0;

        // Làm tròn 2 chữ số thập phân và thêm "%"
        DecimalFormat df = new DecimalFormat("0.##");
        String formattedRate = df.format(rate) + "%";

        orderStats.put("conversionRate", formattedRate);

        return orderStats;
    }

    @Override
    public AdminOrderDisplayDTO findOrderDetailById(Integer orderId) {
        return orderRepository.findOrderDetailById(orderId)
                .stream()
                .map(this::convertToAdminOrderDisplayDTO)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<OrderItemDisplayDTO> getOrderItemsByOrderId(Integer orderId) {
        return orderRepository.findOrderItemsByOrderId(orderId)
                .stream()
                .map(this::convertToOrderCustomerDisplayDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateOrderStatus(Integer orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setOrderStatus(status);
        orderRepository.save(order);
    }

    @Override
    public void updateShippingAddress(Integer orderId, String shippingAddress) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setShippingAddress(shippingAddress);
        orderRepository.save(order);
    }

    // Lấy tổng chi tiêu của khách hàng theo tháng và năm
    @Override
    public BigDecimal getTotalSpentByCustomerInMonthAndYear(Integer customerId, int month, int year) {
        return orderRepository.getTotalSpendingByMonthAndYear(customerId, month, year);
    }

    // Lấy tổng chi tiêu của khách hàng theo năm
    @Override
    public BigDecimal getTotalSpentByCustomerInYear(Integer customerId, int year) {
        return orderRepository.getTotalSpendingByYear(customerId, year);
    }

    // Lấy danh sách sản phẩm đã mua của khách hàng theo id
    @Override
    public List<ProductPurchaseInfoDTO> findPurchasedProductsByCustomerId(Integer customerId) {
        return orderRepository.findPurchasedProductsByCustomerId(customerId);
    }

    private OrderItemDisplayDTO convertToOrderCustomerDisplayDTO(Object[] result) {
        return new OrderItemDisplayDTO(
                (String) result[0],                           // imageUrl
                (String) result[1],                           // productName
                ((Number) result[2]).intValue(),              // volume
                (BigDecimal) result[3],                       // price
                ((Number) result[4]).intValue()               // quantity
        );
    }

    private AdminOrderDisplayDTO convertToAdminOrderDisplayDTO(Object[] result) {
        return new AdminOrderDisplayDTO(
                (Integer) result[0],                             // orderId
                (String) result[5],                              // customerName
                (String) result[6],                              // email
                (String) result[7],                              // phone
                (BigDecimal) result[1],                          // totalAmount
                (String) result[2],                              // orderStatus
                toLocalDate(result[3]),                          // orderDate
                ((Number) result[9]).intValue(),                 // itemCount
                (String) result[8],                              // paymentMethod
                (String) result[4]                               // shippingAddress
        );
    }

    private LocalDate toLocalDate(Object dateObj) {
        if (dateObj instanceof java.sql.Date) {
            return ((java.sql.Date) dateObj).toLocalDate();
        } else if (dateObj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) dateObj).toLocalDateTime().toLocalDate();
        } else if (dateObj instanceof java.util.Date) {
            return ((java.util.Date) dateObj).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        return null;
    }


    private TopSellingDisplayDTO convertToTopSellingDisplayDTO(Object[] result) {
        return new TopSellingDisplayDTO(
                (Integer) result[0],
                (String) result[1],
                (Integer) result[2],
                (Integer) result[3],
                (BigDecimal) result[4]
        );
    }

    private RecentOrderDTO convertToRecentOrderDTO(Order order) {
        return new RecentOrderDTO(
                order.getOrderId(),
                order.getCustomer().getCustomerId(),
                order.getOrderDate(),
                order.getOrderStatus(),
                order.getTotalAmount()
        );
    }

    private OrderDTO convertToOrderDTO(Order order) {
        return new OrderDTO(
                order.getOrderId(),
                order.getCustomer().getCustomerId(),
                order.getEmployee() != null ? order.getEmployee().getEmployeeId() : null,
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getShippingFee(),
                order.getOrderStatus(),
                order.getShippingAddress(),
                order.getShippingOption(),
                order.getNote(),
                order.getEstimatedDeliveryDate()
        );
    }
}
