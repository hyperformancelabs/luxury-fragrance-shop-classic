package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.dto.ProductPurchaseInfoDTO;
import com.hyperformancelabs.backend.dto.TopSellingDisplayDTO;
import com.hyperformancelabs.backend.model.Customer;
import com.hyperformancelabs.backend.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
//    List<Order> findByCustomer(Customer customer);
//
    Order findByOrderId(Integer orderId);

    // Lấy danh sách đơn hàng của khách hàng sắp xếp theo ngày đặt giảm dần
    List<Order> findByCustomer_CustomerIdOrderByOrderDateDesc(Integer customerId);

    // lấy danh sách đơn hàng theo số điện thoại
    List<Order> findByCustomer_PhoneNumber(String phone);
//
//    @Query("""
//    SELECT o FROM Order o
//    LEFT JOIN FETCH o.orderItems oi
//    LEFT JOIN FETCH oi.productVariant pv
//    LEFT JOIN FETCH pv.product p
//    LEFT JOIN FETCH p.brand
//    WHERE o.orderId = :orderId
//""")
//    Optional<Order> findByIdWithItems(@Param("orderId") Integer orderId);
//
//    @Query(value = """
//    SELECT COALESCE(SUM(o.total_amount), 0)
//    FROM [Order] o
//    WHERE o.order_status = 'delivered'
//    AND o.order_date >= CAST(GETDATE() AS DATE)
//    AND o.order_date < DATEADD(DAY, 1, CAST(GETDATE() AS DATE))
//    """, nativeQuery = true)
//    BigDecimal getTotalAmountOfDeliveredOrdersToday();
//
//    @Query(value = """
//    SELECT COALESCE(SUM(o.total_amount), 0)
//    FROM [Order] o
//    WHERE o.order_status = 'delivered'
//      AND o.order_date >= DATEADD(DAY, 1, EOMONTH(GETDATE(), -1))
//      AND o.order_date <= DATEADD(DAY, 7 - DATEPART(WEEKDAY, GETDATE()) +
//            CASE WHEN DATEPART(WEEKDAY, GETDATE()) = 1 THEN -6 ELSE 1 END, CAST(GETDATE() AS DATE))
//    """, nativeQuery = true)
//    BigDecimal getTotalAmountOfDeliveredOrdersInCurrentWeek();
//
//    @Query(value = """
//    SELECT COALESCE(SUM(o.total_amount), 0)
//    FROM [Order] o
//    WHERE o.order_status = 'delivered'
//      AND YEAR(o.order_date) = YEAR(GETDATE())
//      AND MONTH(o.order_date) = MONTH(GETDATE())
//    """, nativeQuery = true)
//    BigDecimal getTotalAmountOfDeliveredOrdersInCurrentMonth();
//
//    @Query(value = """
//    SELECT COALESCE(SUM(o.total_amount), 0)
//    FROM [Order] o
//    WHERE o.order_status = 'delivered'
//      AND YEAR(o.order_date) = YEAR(GETDATE())
//    """, nativeQuery = true)
//    BigDecimal getTotalAmountOfDeliveredOrdersInCurrentYear();
//
//    @Query(value = """
//    SELECT COALESCE(SUM(o.total_amount), 0)
//    FROM [Order] o
//    WHERE o.order_status = 'delivered'
//    AND o.order_date >= DATEFROMPARTS(:year, :month, 1)
//    AND o.order_date < DATEFROMPARTS(
//        CASE
//            WHEN :month = 12 THEN :year + 1
//            ELSE :year
//        END,
//        CASE
//            WHEN :month = 12 THEN 1
//            ELSE :month + 1
//        END,
//        1
//    )
//    """, nativeQuery = true)
//    BigDecimal getTotalAmountOfDeliveredOrdersByMonthAndYear(
//        @Param("month") int month,
//        @Param("year") int year
//    );
//
//    @Query(value = """
//    SELECT COALESCE(SUM(o.total_amount), 0)
//    FROM [Order] o
//    WHERE o.order_status = 'delivered'
//    AND o.order_date >= CONVERT(DATETIME, :startDate, 103)
//    AND o.order_date < DATEADD(DAY, 1, CONVERT(DATETIME, :endDate, 103))
//    """, nativeQuery = true)
//    BigDecimal getTotalAmountOfDeliveredOrdersByDateRange(
//        @Param("startDate") String startDate,  // format: dd/MM/yyyy
//        @Param("endDate") String endDate       // format: dd/MM/yyyy
//    );
//
//    @Query(value = """
//    SELECT COALESCE(SUM(o.total_amount), 0)
//    FROM [Order] o
//    WHERE o.order_status = 'delivered'
//    AND o.order_date >= DATEFROMPARTS(:year,
//        CASE :quarter
//            WHEN 1 THEN 1
//            WHEN 2 THEN 4
//            WHEN 3 THEN 7
//            WHEN 4 THEN 10
//        END,
//        1)
//    AND o.order_date < DATEFROMPARTS(
//        CASE
//            WHEN :quarter = 4 THEN :year + 1
//            ELSE :year
//        END,
//        CASE :quarter
//            WHEN 1 THEN 4
//            WHEN 2 THEN 7
//            WHEN 3 THEN 10
//            WHEN 4 THEN 1
//        END,
//        1)
//    """, nativeQuery = true)
//    BigDecimal getTotalAmountOfDeliveredOrdersByQuarterAndYear(
//        @Param("quarter") int quarter,  // 1-4
//        @Param("year") int year
//    );

    // ------------------------------------------------- ADMIN -----------------------------------------------------

    // Tổng doanh thu ngày hôm nay
    @Query(value = """
        SELECT COALESCE(SUM(o.total_amount), 0)
        FROM [Order] o
        WHERE (o.order_status = 'delivered' OR o.order_status = 'shipping')
        AND CAST(o.order_date AS DATE) = CAST(GETDATE() AS DATE)
    """, nativeQuery = true)
    BigDecimal getTotalRevenueToday();

    // Tổng doanh thu tuần hiện tại
    @Query(value = """
    SELECT COALESCE(SUM(o.total_amount), 0)
    FROM [Order] o
    WHERE (o.order_status = 'delivered' OR o.order_status = 'shipping')
    AND DATEPART(ISO_WEEK, o.order_date) = DATEPART(ISO_WEEK, GETDATE())
    AND YEAR(o.order_date) = YEAR(GETDATE())
""", nativeQuery = true)
    BigDecimal getTotalRevenueThisWeek();


    // Tổng doanh thu tháng hiện tại
    @Query(value = """
        SELECT COALESCE(SUM(o.total_amount), 0)
        FROM [Order] o
        WHERE (o.order_status = 'delivered' OR o.order_status = 'shipping')
        AND MONTH(o.order_date) = MONTH(GETDATE())
        AND YEAR(o.order_date) = YEAR(GETDATE())
    """, nativeQuery = true)
    BigDecimal getTotalRevenueCurrentMonth();

    // Năm hiện tại
    @Query(value = """
        SELECT COALESCE(SUM(o.total_amount), 0)
        FROM [Order] o
        WHERE (o.order_status = 'delivered' OR o.order_status = 'shipping')
        AND YEAR(o.order_date) = YEAR(GETDATE())
    """, nativeQuery = true)
    BigDecimal getTotalRevenueCurrentYear();

    // Tổng doanh thu theo ngày
    @Query(value = """
    SELECT COALESCE(SUM(o.total_amount), 0)
    FROM [Order] o
    WHERE (o.order_status = 'delivered' OR o.order_status = 'shipping')
    AND CAST(o.order_date AS DATE) = :targetDate
""", nativeQuery = true)
    BigDecimal getTotalRevenueByDate(@Param("targetDate") LocalDate targetDate);

    // Tổng doanh thu các ngày trong tháng
    @Query(value = """
    SELECT CAST(o.order_date AS DATE) AS orderDay, SUM(o.total_amount)
    FROM [Order] o
    WHERE (o.order_status = 'delivered' OR o.order_status = 'shipping')
      AND MONTH(o.order_date) = :month
      AND YEAR(o.order_date) = :year
    GROUP BY CAST(o.order_date AS DATE)
""", nativeQuery = true)
    List<Object[]> getDailyRevenueInMonth(@Param("month") int month, @Param("year") int year);



    // Tổng doanh thu theo tháng và năm
    @Query(value = """
        SELECT COALESCE(SUM(o.total_amount), 0)
        FROM [Order] o
        WHERE (o.order_status = 'delivered' OR o.order_status = 'shipping')
        AND MONTH(o.order_date) = :month
        AND YEAR(o.order_date) = :year
    """, nativeQuery = true)
    BigDecimal getTotalRevenueByMonthAndYear(@Param("month") int month, @Param("year") int year);

    // Tổng doanh thu theo ngày tháng năm
    @Query(value = """
        SELECT coalesce(SUM(o.total_amount), 0)
        FROM [Order] o
        WHERE (o.order_status = 'delivered' OR o.order_status = 'shipping')
        AND CAST(o.order_date AS DATE) BETWEEN :startDate AND :endDate
    """, nativeQuery = true)
    BigDecimal getTotalRevenueBetweenDates(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    // Tổng doanh thu theo loại sản phẩm
    @Query(value = """
        WITH GenderList AS (
            SELECT 'Men' AS suitable_gender
            UNION ALL
            SELECT 'Women'
            UNION ALL
            SELECT 'Unisex'
        ),
        GenderRevenue AS (
            SELECT 
                pd.detail_value AS suitable_gender,
                SUM(oi.quantity * oi.unit_price) AS total_revenue
            FROM 
                OrderItem oi
            JOIN [Order] o ON oi.order_id = o.order_id
            JOIN ProductVariant pv ON oi.product_variant_id = pv.product_variant_id
            JOIN Product p ON pv.product_id = p.product_id
            JOIN ProductDetail pd ON p.product_id = pd.product_id
            WHERE 
                pd.detail_name = 'suitable_gender'
                AND pd.detail_value IN ('Men', 'Women', 'Unisex')
                AND YEAR(o.order_date) = :year
                AND o.order_status IN ('delivered', 'shipping')
            GROUP BY 
                pd.detail_value
        )
        SELECT 
            g.suitable_gender,
            ISNULL(r.total_revenue, 0) AS total_revenue
        FROM 
            GenderList g
        LEFT JOIN 
            GenderRevenue r ON g.suitable_gender = r.suitable_gender
        ORDER BY 
            total_revenue DESC
    """, nativeQuery = true)
    List<Object[]> getRevenueBySuitableGender(@Param("year") int year);

    // Tổng doanh thu theo thương hiệu sản phẩm
    @Query(value = """
        DECLARE @year INT = :year;

        WITH RevenueByBrand AS (
            SELECT 
                b.brand_name,
                SUM(o.total_amount) AS revenue
            FROM [Order] o
            JOIN OrderItem oi ON o.order_id = oi.order_id
            JOIN ProductVariant pv ON oi.product_variant_id = pv.product_variant_id
            JOIN Product p ON pv.product_id = p.product_id
            JOIN Brand b ON p.brand_id = b.brand_id
            WHERE o.order_status IN ('delivered', 'shipping')
              AND YEAR(o.order_date) = @year
            GROUP BY b.brand_name
        ),
        TotalRevenue AS (
            SELECT SUM(revenue) AS total FROM RevenueByBrand
        ),
        RankedBrands AS (
            SELECT 
                brand_name,
                revenue,
                SUM(revenue) OVER (ORDER BY revenue DESC ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS cumulative_revenue
            FROM RevenueByBrand
        ),
        SelectedBrands AS (
            SELECT 
                brand_name,
                revenue
            FROM RankedBrands, TotalRevenue
            WHERE cumulative_revenue <= total * 0.7
            UNION ALL
            SELECT TOP 1 brand_name, revenue
            FROM RankedBrands, TotalRevenue
            WHERE cumulative_revenue > total * 0.7
            ORDER BY cumulative_revenue
        ),
        Others AS (
            SELECT 
                'Khác' AS brand_name,
                (SELECT total FROM TotalRevenue) - SUM(revenue) AS revenue
            FROM SelectedBrands
        )
        -- Final result
        SELECT brand_name, revenue FROM SelectedBrands
        UNION ALL
        SELECT brand_name, revenue FROM Others
        """, nativeQuery = true)
    List<Object[]> getTopBrandsDominatingRevenue(@Param("year") int year);

    // Top 5 sản phẩm bán chạy nhất
    @Query(value = """
        SELECT TOP 5
            p.product_id AS productId,
            p.product_name AS productName,
            pv.volume AS volume,
            SUM(oi.quantity) AS totalQuantity,
            SUM(oi.quantity * pv.price) AS revenue
        FROM [Order] o
        JOIN OrderItem oi ON o.order_id = oi.order_id
        JOIN ProductVariant pv ON oi.product_variant_id = pv.product_variant_id
        JOIN Product p ON pv.product_id = p.product_id
        WHERE o.order_status IN ('delivered', 'shipping')
        GROUP BY p.product_id, p.product_name, pv.volume
        ORDER BY SUM(oi.quantity * pv.price) DESC
    """, nativeQuery = true)
    List<Object[]> findTop5SellingVariants();

    // Số đơn hàng hôm nay
    @Query(value = """
        SELECT COUNT_BIG(o.order_id)
        FROM [Order] o
        WHERE o.order_status IN ('pending', 'processing', 'shipping', 'delivered')
        AND CAST(o.order_date AS DATE) = CAST(GETDATE() AS DATE)
    """, nativeQuery = true)
    Long countOrdersToday();

    // Số đơn hàng trong tháng hiện tại
    @Query(value = """
        SELECT COUNT_BIG(o.order_id)
        FROM [Order] o
        WHERE o.order_status IN ('pending', 'processing', 'shipping', 'delivered')
        AND MONTH(o.order_date) = MONTH(GETDATE())
        AND YEAR(o.order_date) = YEAR(GETDATE())
    """, nativeQuery = true)
    Long countOrdersThisMonth();

    // Năm hiện tại
    @Query(value = """
        SELECT COUNT_BIG(o.order_id)
        FROM [Order] o
        WHERE o.order_status IN ('pending', 'processing', 'shipping', 'delivered')
        AND YEAR(o.order_date) = YEAR(GETDATE())
    """, nativeQuery = true)
    Long countOrdersThisYear();

    // Số đơn hàng theo tháng và năm
    @Query(value = """
        SELECT COUNT_BIG(o.order_id)
        FROM [Order] o
        WHERE o.order_status IN ('pending', 'processing', 'shipping', 'delivered')
        AND MONTH(o.order_date) = :month
        AND YEAR(o.order_date) = :year
    """, nativeQuery = true)
    Long countOrdersByMonthAndYear(@Param("month") int month, @Param("year") int year);

    // Số đơn hàng trong khoảng ngày
    @Query(value = """
        SELECT COUNT_BIG(o.order_id)
        FROM [Order] o
        WHERE o.order_status IN ('pending', 'processing', 'shipping', 'delivered')
        AND o.order_date BETWEEN :startDate AND :endDate
    """, nativeQuery = true)
    Long countOrdersBetweenDates(@Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    // Lấy 3 đơn hàng mới nhất
    @Query(value = """
    SELECT TOP 3 *
    FROM [Order]
    ORDER BY order_date DESC
    """, nativeQuery = true)
    List<Order> findTop3ByOrderByOrderDateDesc();

    // Lấy đơn hàng theo trạng thái và khoảng thời gian
    @Query(value = """
    SELECT 
        o.order_id,
        o.total_amount,
        o.order_status,
        o.order_date,
        o.shipping_address,
        c.name AS customer_name,
        c.email AS customer_email,
        c.phone_number AS customer_phone,
        pm.method_name AS payment_method,
        SUM(oi.quantity) AS total_quantity
    FROM [Order] o
    JOIN Customer c ON o.customer_id = c.customer_id
    LEFT JOIN Payment p ON o.order_id = p.order_id
    LEFT JOIN PaymentMethod pm ON p.payment_method_id = pm.payment_method_id
    LEFT JOIN OrderItem oi ON o.order_id = oi.order_id
    WHERE 
        (:status IS NULL OR o.order_status = :status)
        AND (:startDate IS NULL OR o.order_date >= :startDate)
        AND (:endDate IS NULL OR o.order_date <= :endDate)
        AND (
            :keyword IS NULL OR
            o.order_id LIKE '%' + :keyword + '%' OR
            c.name LIKE '%' + :keyword + '%' OR
            c.email LIKE '%' + :keyword + '%' OR
            c.phone_number LIKE '%' + :keyword + '%'
        )
    GROUP BY 
        o.order_id, o.total_amount, o.order_status, o.order_date, o.shipping_address,
        c.name, c.email, c.phone_number,
        pm.method_name
    ORDER BY o.order_date DESC
    """, nativeQuery = true)
    Page<Object[]> findOrdersByDateAndStatus(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );


    // Lấy tổng số đơn thống kê
    @Query(value = """
        SELECT COUNT(*) FROM [Order]
        UNION ALL
        SELECT COUNT(*) FROM [Order] WHERE order_status = 'pending'
        UNION ALL
        SELECT COUNT(*) FROM [Order] WHERE order_status = 'delivered'
        """, nativeQuery = true)
    List<Long> getOrderStatistics();

    @Query(value = """
    SELECT 
        o.order_id,
        o.total_amount,
        o.order_status,
        o.order_date,
        o.shipping_address,
        c.name AS customer_name,
        c.email AS customer_email,
        c.phone_number AS customer_phone,
        pm.method_name AS payment_method,
        SUM(oi.quantity) AS total_quantity
    FROM [Order] o
    JOIN Customer c ON o.customer_id = c.customer_id
    LEFT JOIN Payment p ON o.order_id = p.order_id
    LEFT JOIN PaymentMethod pm ON p.payment_method_id = pm.payment_method_id
    LEFT JOIN OrderItem oi ON o.order_id = oi.order_id
    WHERE o.order_id = :orderId
    GROUP BY 
        o.order_id, o.total_amount, o.order_status, o.order_date, o.shipping_address,
        c.name, c.email, c.phone_number,
        pm.method_name
    """, nativeQuery = true)
    List<Object[]> findOrderDetailById(@Param("orderId") Integer orderId);

    @Query(value = """
    SELECT 
        p.image_url, 
        p.product_name, 
        pv.volume, 
        oi.unit_price, 
        oi.quantity
    FROM OrderItem oi
    JOIN ProductVariant pv ON oi.product_variant_id = pv.product_variant_id
    JOIN Product p ON pv.product_id = p.product_id
    WHERE oi.order_id = :orderId
    """, nativeQuery = true)
    List<Object[]> findOrderItemsByOrderId(@Param("orderId") Integer orderId);

    // Lấy tổng chi tiêu của khách hàng theo tháng và năm
    @Query("""
    SELECT COALESCE(SUM(o.totalAmount), 0)
    FROM Order o
    WHERE o.customer.customerId = :customerId
      AND o.orderStatus IN ('delivered', 'shipping')
      AND FUNCTION('MONTH', o.orderDate) = :month
      AND FUNCTION('YEAR', o.orderDate) = :year
""")
    BigDecimal getTotalSpendingByMonthAndYear(
            @Param("customerId") Integer customerId,
            @Param("month") int month,
            @Param("year") int year
    );

    // Lấy tổng chi tiêu theo năm
    @Query("""
    SELECT COALESCE(SUM(o.totalAmount), 0)
    FROM Order o
    WHERE o.customer.customerId = :customerId
      AND o.orderStatus IN ('delivered', 'shipping')
      AND FUNCTION('YEAR', o.orderDate) = :year
""")
    BigDecimal getTotalSpendingByYear(
            @Param("customerId") Integer customerId,
            @Param("year") int year
    );

    @Query(value = """
        SELECT 
            p.product_id AS productId,
            p.product_name AS productName,
            p.image_url AS imageUrl,
            pv.volume AS volume,
            pv.price AS price
        FROM [Order] o
        JOIN OrderItem oi ON o.order_id = oi.order_id
        JOIN ProductVariant pv ON oi.product_variant_id = pv.product_variant_id
        JOIN Product p ON pv.product_id = p.product_id
        WHERE o.customer_id = :customerId
          AND o.order_status != 'cancelled'
        ORDER BY o.order_date DESC
    """, nativeQuery = true)
    List<ProductPurchaseInfoDTO> findPurchasedProductsByCustomerId(@Param("customerId") Integer customerId);

}
