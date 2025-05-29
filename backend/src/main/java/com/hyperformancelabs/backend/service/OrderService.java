package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface OrderService {
    // Tìm đơn hàng theo ID
    OrderDTO findOrderById(Integer orderId);

    // Tìm đơn hàng theo ID của khách hàng
    List<OrderDTO> findOrdersByCustomerId(Integer customerId);

    // Lấy danh sách đơn hàng theo số điện thoại
    List<OrderDTO> findByPhone(String phone);

    // ----------------------------------------------- ADMIN -----------------------------------------------------

    // Tổng doanh thu ngày hôm nay
    BigDecimal getTotalRevenueToday();

    // Tổng doanh thu tuần hiện tại
    BigDecimal getTotalRevenueThisWeek();

    // Tổng doanh thu tháng hiện tại
    BigDecimal getTotalRevenueCurrentMonth();

    // Tổng doanh thu năm hiện tại
    BigDecimal getTotalRevenueCurrentYear();

    // Tổng doanh thu theo ngày
    BigDecimal getTotalRevenueByDate(LocalDate targetDate);

    // Tổng doanh thu theo tháng và năm
    BigDecimal getTotalRevenueByMonthAndYear(int month, int year);

    // Tổng doanh thu theo ngày tháng năm
    BigDecimal getTotalRevenueBetweenDates(LocalDate startDate, LocalDate endDate);

    // Tổng số đơn hàng ngày hôm nay
    Long countOrdersToday();

    // Tổng số đơn hàng tháng hiện tại
    Long countOrdersThisMonth();

    // Tổng số đơn hàng năm hiện tại
    Long countOrdersThisYear();

    // Tổng số đơn hàng theo tháng và năm
    Long countOrdersByMonthAndYear(int month, int year);

    // Tổng số đơn hàng theo ngày tháng năm
    Long countOrdersBetweenDates(LocalDate startDate, LocalDate endDate);

    // Lấy 3 đơn hàng mới nhất
    List<RecentOrderDTO> findTop3ByOrderByOrderDateDesc();

    // Lấy doanh thu theo tháng của năm hiện tại
    Map<String, BigDecimal> getMonthlyRevenueTillNowOfCurrentYear();

    // Lấy doanh thu theo năm
    Map<String, BigDecimal> getRevenueByYearRange(int startYear, int endYear);

    // Lấy doanh thu ngày trong tháng
    Map<String, BigDecimal> getDailyRevenueInMonth(int month, int year);

    // Lấy doanh thu tuần trong tháng
    Map<String, BigDecimal> getWeeklyRevenueInMonth(int month, int year);

    Map<String, BigDecimal> getRevenueBySuitableGender(int year);

    Map<String, BigDecimal> getRevenueByBrand(int year);

    List<TopSellingDisplayDTO> findTop5SellingVariants();

    // Lấy đơn hàng theo trạng thái và khoảng thời gian
    Page<AdminOrderDisplayDTO> findOrdersByDateAndStatus(String keyword, String status, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Lấy số đơn thông kê
    Map<String, String> getOrderStatistics();

    // Lấy thông tin đơn hàng theo id
    AdminOrderDisplayDTO findOrderDetailById(Integer orderId);

    // Lấy dánh sách sản phẩm của đơn hàng
    List<OrderItemDisplayDTO> getOrderItemsByOrderId(Integer orderId);

    // Cập nhật trạng thái đơn hàng
    void updateOrderStatus(Integer orderId, String status);

    // Cập nhật địa chỉ giao hàng
    void updateShippingAddress(Integer orderId, String shippingAddress);

    // Lấy tổng chi tiêu của khách hàng theo tháng và năm
    BigDecimal getTotalSpentByCustomerInMonthAndYear(Integer customerId, int month, int year);

    // Lấy tổng chi tiêu của khách hàng theo năm
    BigDecimal getTotalSpentByCustomerInYear(Integer customerId, int year);

    // Lấy thông tin sản phẩm đã mua của khách hàng theo id
    List<ProductPurchaseInfoDTO> findPurchasedProductsByCustomerId(Integer customerId);
}
