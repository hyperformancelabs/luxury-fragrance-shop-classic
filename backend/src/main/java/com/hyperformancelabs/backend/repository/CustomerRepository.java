package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.model.Cart;
import com.hyperformancelabs.backend.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface  CustomerRepository extends JpaRepository<Customer, Integer> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phone);
    //Kiểm tra trạng thái trạng thái banned và số điện thoại
    boolean existsByPhoneNumberAndStatusNot(String phone, String status);
    Optional<Customer> findTopByUsernameOrderByCustomerIdDesc(String username);
    Optional<Customer> findByCustomerId(Integer customerId);
    List<Customer> findByUsername(String username);
    Optional<Customer> findByEmailOrPhoneNumber(String email, String phone);

    // -------------------------------------- ADMIN -----------------------------------------------------

    // Số khách hàng mới hôm nay
    @Query(value = """
    SELECT COUNT_BIG(*) 
    FROM [Customer] c 
    WHERE CAST(c.create_at AS DATE) = CAST(GETDATE() AS DATE)
""", nativeQuery = true)
    Long countCustomersToday();

    // Số khách hàng mới trong tháng hiện tại
    @Query(value = """
    SELECT COUNT_BIG(*) 
    FROM [Customer] c 
    WHERE MONTH(c.create_at) = MONTH(GETDATE())
      AND YEAR(c.create_at) = YEAR(GETDATE())
""", nativeQuery = true)
    Long countCustomersThisMonth();

    // Số khách hàng mới trong năm hiện tại
    @Query(value = """
    SELECT COUNT_BIG(*) 
    FROM [Customer] c 
    WHERE YEAR(c.create_at) = YEAR(GETDATE())
""", nativeQuery = true)
    Long countCustomersThisYear();

    // Số khách hàng mới theo tháng và năm
    @Query(value = """
    SELECT COUNT_BIG(*) 
    FROM [Customer] c 
    WHERE MONTH(c.create_at) = :month
      AND YEAR(c.create_at) = :year
""", nativeQuery = true)
    Long countCustomersByMonthAndYear(@Param("month") int month, @Param("year") int year);

    // Số khách hàng mới trong khoảng ngày
    @Query(value = """
    SELECT COUNT_BIG(*) 
    FROM [Customer] c 
    WHERE c.create_at BETWEEN :startDate AND :endDate
""", nativeQuery = true)
    Long countCustomersBetweenDates(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    // Lọc khách hàng
    @Query(value = """
    SELECT *
    FROM Customer
    WHERE 
        (:status IS NULL OR status = :status)
        AND (
            :keyword IS NULL OR 
            name LIKE %:keyword% OR 
            phone_number LIKE %:keyword% OR 
            email LIKE %:keyword%
        )
    ORDER BY
        CASE WHEN :sortBy = 'name' AND :sortDir = 'ASC' THEN name END ASC,
        CASE WHEN :sortBy = 'name' AND :sortDir = 'DESC' THEN name END DESC,
        CASE WHEN :sortBy = 'loyalty_points' AND :sortDir = 'ASC' THEN loyalty_points END ASC,
        CASE WHEN :sortBy = 'loyalty_points' AND :sortDir = 'DESC' THEN loyalty_points END DESC,
        CASE WHEN :sortBy = 'create_at' AND :sortDir = 'ASC' THEN create_at END ASC,
        CASE WHEN :sortBy = 'create_at' AND :sortDir = 'DESC' THEN create_at END DESC
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM Customer
    WHERE 
        (:status IS NULL OR status = :status)
        AND (
            :keyword IS NULL OR 
            name LIKE %:keyword% OR 
            phone_number LIKE %:keyword% OR 
            email LIKE %:keyword%
        )
    """,
            nativeQuery = true)
    Page<Customer> findCustomersWithOptionalStatusAndSort(
            @Param("keyword") String keyword,     // Search keyword
            @Param("status") String status,       // active, inactive, banned
            @Param("sortBy") String sortBy,       // name, loyalty_points, create_at
            @Param("sortDir") String sortDir,     // ASC, DESC
            Pageable pageable
    );
}

