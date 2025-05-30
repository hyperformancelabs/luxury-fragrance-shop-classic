package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.dto.admin.response.SellTransactionSummaryDTO;
import com.hyperformancelabs.backend.model.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Integer> {

    // Lấy top 6 giao dịch nhập hàng mới nhất với unique products
    @Query(value = """
            WITH RankedTransactions AS (
                SELECT it.inventory_transaction_id, it.product_variant_id, it.performed_by, 
                       it.transaction_type, it.transaction_date, it.before_quantity, 
                       it.quantity, it.after_quantity, it.reason, it.note, it.cost_price,
                       ROW_NUMBER() OVER (PARTITION BY pv.product_id ORDER BY it.transaction_date DESC) as rn
                FROM [InventoryTransaction] it
                JOIN [ProductVariant] pv ON it.product_variant_id = pv.product_variant_id
                WHERE it.transaction_type = 'IMPORT'
            )
            SELECT TOP 6 inventory_transaction_id, product_variant_id, performed_by, 
                   transaction_type, transaction_date, before_quantity, 
                   quantity, after_quantity, reason, note, cost_price
            FROM RankedTransactions
            WHERE rn = 1
            ORDER BY transaction_date DESC
    """, nativeQuery = true)
    List<InventoryTransaction> findTop6ImportTransactionsNative();

    // Tuần hiện tại
    @Query(value = """
        SELECT 
            COUNT(*) AS totalSellTransactions,
            ISNULL(SUM(cost_price), 0) AS totalCostPrice
        FROM InventoryTransaction
        WHERE transaction_type = 'sell'
          AND transaction_date >= DATEADD(DAY, -(DATEPART(WEEKDAY, GETDATE()) + 5) % 7, CAST(GETDATE() AS DATE))
          AND transaction_date <= DATEADD(DAY, 6 - (DATEPART(WEEKDAY, GETDATE()) + 5) % 7, CAST(GETDATE() AS DATE))
        """, nativeQuery = true)
    SellTransactionSummaryDTO getSellTransactionSummaryInCurrentWeek();

    // Ngày hôm nay
    @Query(value = """
        SELECT 
            COUNT(*) AS totalSellTransactions,
            ISNULL(SUM(cost_price), 0) AS totalCostPrice
        FROM InventoryTransaction
        WHERE transaction_type = 'sell'
          AND CAST(transaction_date AS DATE) = CAST(GETDATE() AS DATE)
        """, nativeQuery = true)
    SellTransactionSummaryDTO getSellTransactionSummaryInToday();

    // Tháng này
    @Query(value = """
        SELECT 
            COUNT(*) AS totalSellTransactions,
            ISNULL(SUM(cost_price), 0) AS totalCostPrice
        FROM InventoryTransaction
        WHERE transaction_type = 'sell'
          AND YEAR(transaction_date) = YEAR(GETDATE())
          AND MONTH(transaction_date) = MONTH(GETDATE())
        """, nativeQuery = true)
    SellTransactionSummaryDTO getSellTransactionSummaryInCurrentMonth();

    // Năm này
    @Query(value = """
        SELECT 
            COUNT(*) AS totalSellTransactions,
            ISNULL(SUM(cost_price), 0) AS totalCostPrice
        FROM InventoryTransaction
        WHERE transaction_type = 'sell'
          AND YEAR(transaction_date) = YEAR(GETDATE())
        """, nativeQuery = true)
    SellTransactionSummaryDTO getSellTransactionSummaryInCurrentYear();

    @Query(value = """
        SELECT 
            COUNT(1) AS totalSellTransactions,
            COALESCE(SUM(cost_price), 0) AS totalCostPrice
        FROM InventoryTransaction
        WHERE transaction_type = 'sell'
        AND CAST(transaction_date AS DATE) BETWEEN CAST(:startDate AS DATE) AND CAST(:endDate AS DATE)
        """, nativeQuery = true)
    SellTransactionSummaryDTO getSellTransactionSummaryByDateRange(
        @Param("startDate") String startDate,  // format: yyyy-MM-dd
        @Param("endDate") String endDate       // format: yyyy-MM-dd
    );

    @Query(value = """
        SELECT 
            COUNT(1) AS totalSellTransactions,
            COALESCE(SUM(cost_price), 0) AS totalCostPrice
        FROM InventoryTransaction
        WHERE transaction_type = 'sell'
        AND YEAR(transaction_date) = :year
        AND DATEPART(QUARTER, transaction_date) = :quarter
        """, nativeQuery = true)
    SellTransactionSummaryDTO getSellTransactionSummaryByQuarterAndYear(
        @Param("quarter") int quarter,  // 1-4
        @Param("year") int year
    );
}
