package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.dto.FlashSaleProductDTO;
import com.hyperformancelabs.backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    // Tìm sản phẩm theo tên thương hiệu với phân trang
    Page<Product> findByBrand_BrandName(String brandName, Pageable pageable);
    
    // Tìm sản phẩm theo tên sản phẩm với phân trang
    Page<Product> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);
    
    // Tìm sản phẩm cùng thương hiệu, ngoại trừ sản phẩm hiện tại
    Page<Product> findByBrand_BrandIdAndProductIdNot(Integer brandId, Integer productId, Pageable pageable);
    
    // Lấy top sản phẩm bán chạy
    @Query(value = "SELECT p.* FROM [Product] p " +
           "JOIN [ProductVariant] pv ON p.product_id = pv.product_id " +
           "JOIN [OrderItem] oi ON pv.product_variant_id = oi.product_variant_id " +
           "GROUP BY p.product_id, p.brand_id, p.product_name, p.description, p.image_url " +
           "ORDER BY SUM(oi.quantity) DESC", 
           nativeQuery = true)
    List<Product> findTopSellingProducts(Pageable pageable);
    
    default List<Product> findTopSellingProducts(int limit) {
        return findTopSellingProducts(PageRequest.of(0, limit));
    }

    // Lấy danh sách sản phẩm đang trong flash sale
    @Query(value = "SELECT p.product_id, p.product_name, " +
            "pp.product_promotion_id, pp.max_discount_amount, pp.condition_json, " +
            "pr.promotion_name, pr.discount_type, pr.discount_value, pr.usage_limit, " +
            "pp.start_date, pp.end_date " +
            "FROM ProductPromotion pp " +
            "JOIN Product p ON pp.product_id = p.product_id " +
            "JOIN Promotion pr ON pp.promotion_id = pr.promotion_id " +
            "WHERE pp.status = 'active' " +
            "AND GETDATE() BETWEEN pp.start_date AND ISNULL(pp.end_date, '9999-12-31') " +
            "AND pr.status = 'active'",
            nativeQuery = true)
    List<Object[]> findActiveFlashSaleProducts();

    // Lọc sản phẩm
    @Query(value = """
    SELECT DISTINCT p.*
    FROM Product p
    JOIN Brand b ON p.brand_id = b.brand_id
    LEFT JOIN ProductDetail pd ON p.product_id = pd.product_id
    LEFT JOIN ProductVariant pv ON p.product_id = pv.product_id
    WHERE 
        (:genderList IS NULL OR (
            pd.detail_name = 'suitable_gender' AND 
            EXISTS (
                SELECT 1 FROM STRING_SPLIT(:genderList, ',') s 
                WHERE LTRIM(RTRIM(s.value)) = pd.detail_value
            )
        ))
        AND (:brandList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:brandList, ',') s 
            WHERE LTRIM(RTRIM(s.value)) = b.brand_name
        ))
        AND ((:minPrice IS NULL OR :maxPrice IS NULL) 
             OR pv.price BETWEEN :minPrice AND :maxPrice)
        AND (:seasonList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:seasonList, ',') s 
            WHERE p.description LIKE '%' + LTRIM(RTRIM(s.value)) + '%'
        ))
    ORDER BY p.product_id
    """,
            countQuery = """
    SELECT COUNT(DISTINCT p.product_id)
    FROM Product p
    JOIN Brand b ON p.brand_id = b.brand_id
    LEFT JOIN ProductDetail pd ON p.product_id = pd.product_id
    LEFT JOIN ProductVariant pv ON p.product_id = pv.product_id
    WHERE 
        (:genderList IS NULL OR (
            pd.detail_name = 'suitable_gender' AND 
            EXISTS (
                SELECT 1 FROM STRING_SPLIT(:genderList, ',') s 
                WHERE LTRIM(RTRIM(s.value)) = pd.detail_value
            )
        ))
        AND (:brandList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:brandList, ',') s 
            WHERE LTRIM(RTRIM(s.value)) = b.brand_name
        ))
        AND ((:minPrice IS NULL OR :maxPrice IS NULL) 
             OR pv.price BETWEEN :minPrice AND :maxPrice)
        AND (:seasonList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:seasonList, ',') s 
            WHERE p.description LIKE '%' + LTRIM(RTRIM(s.value)) + '%'
        ))
    """,
            nativeQuery = true)
    Page<Product> findFilteredProducts(
            @Param("genderList") String genderList,
            @Param("brandList") String brandList,
            @Param("seasonList") String seasonList,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    // Lọc sản phẩm theo giá min
    @Query(value = """
    SELECT DISTINCT p.*, pvmin.min_price
    FROM Product p
    JOIN Brand b ON p.brand_id = b.brand_id
    LEFT JOIN ProductDetail pd ON p.product_id = pd.product_id
    LEFT JOIN (
        SELECT product_id, MIN(price) AS min_price
        FROM ProductVariant
        GROUP BY product_id
    ) pvmin ON p.product_id = pvmin.product_id
    LEFT JOIN ProductVariant pv ON p.product_id = pv.product_id
    
    WHERE 
        (:genderList IS NULL OR (
            pd.detail_name = 'suitable_gender' AND 
            EXISTS (
                SELECT 1 FROM STRING_SPLIT(:genderList, ',') s 
                WHERE LTRIM(RTRIM(s.value)) = pd.detail_value
            )
        ))
        AND (:brandList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:brandList, ',') s 
            WHERE LTRIM(RTRIM(s.value)) = b.brand_name
        ))
        AND ((:minPrice IS NULL OR :maxPrice IS NULL) 
             OR pv.price BETWEEN :minPrice AND :maxPrice)
        AND (:seasonList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:seasonList, ',') s 
            WHERE p.description LIKE '%' + LTRIM(RTRIM(s.value)) + '%'
        ))
        
    ORDER BY pvmin.min_price ASC
    """,

            countQuery = """
    SELECT COUNT(DISTINCT p.product_id)
    FROM Product p
    JOIN Brand b ON p.brand_id = b.brand_id
    LEFT JOIN ProductDetail pd ON p.product_id = pd.product_id
    LEFT JOIN ProductVariant pv ON p.product_id = pv.product_id
    
    WHERE 
        (:genderList IS NULL OR (
            pd.detail_name = 'suitable_gender' AND 
            EXISTS (
                SELECT 1 FROM STRING_SPLIT(:genderList, ',') s 
                WHERE LTRIM(RTRIM(s.value)) = pd.detail_value
            )
        ))
        AND (:brandList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:brandList, ',') s 
            WHERE LTRIM(RTRIM(s.value)) = b.brand_name
        ))
        AND ((:minPrice IS NULL OR :maxPrice IS NULL) 
             OR pv.price BETWEEN :minPrice AND :maxPrice)
        AND (:seasonList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:seasonList, ',') s 
            WHERE p.description LIKE '%' + LTRIM(RTRIM(s.value)) + '%'
        ))
    """,
            nativeQuery = true)
    Page<Product> filterOrderByMinVariantPriceAsc(
            @Param("genderList") String genderList,
            @Param("brandList") String brandList,
            @Param("seasonList") String seasonList,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    // Lọc sản phẩm theo giá max
    @Query(value = """
    SELECT DISTINCT p.*, pvmax.max_price
    FROM Product p
    JOIN Brand b ON p.brand_id = b.brand_id
    LEFT JOIN ProductDetail pd ON p.product_id = pd.product_id
    LEFT JOIN (
        SELECT product_id, MAX(price) AS max_price
        FROM ProductVariant
        GROUP BY product_id
    ) pvmax ON p.product_id = pvmax.product_id
    LEFT JOIN ProductVariant pv ON p.product_id = pv.product_id

    WHERE 
        (:genderList IS NULL OR (
            pd.detail_name = 'suitable_gender' AND 
            EXISTS (
                SELECT 1 FROM STRING_SPLIT(:genderList, ',') s 
                WHERE LTRIM(RTRIM(s.value)) = pd.detail_value
            )
        ))
        AND (:brandList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:brandList, ',') s 
            WHERE LTRIM(RTRIM(s.value)) = b.brand_name
        ))
        AND ((:minPrice IS NULL OR :maxPrice IS NULL) 
             OR pv.price BETWEEN :minPrice AND :maxPrice)
        AND (:seasonList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:seasonList, ',') s 
            WHERE p.description LIKE '%' + LTRIM(RTRIM(s.value)) + '%'
        ))

    ORDER BY pvmax.max_price DESC
    """,

            countQuery = """
    SELECT COUNT(DISTINCT p.product_id)
    FROM Product p
    JOIN Brand b ON p.brand_id = b.brand_id
    LEFT JOIN ProductDetail pd ON p.product_id = pd.product_id
    LEFT JOIN ProductVariant pv ON p.product_id = pv.product_id

    WHERE 
        (:genderList IS NULL OR (
            pd.detail_name = 'suitable_gender' AND 
            EXISTS (
                SELECT 1 FROM STRING_SPLIT(:genderList, ',') s 
                WHERE LTRIM(RTRIM(s.value)) = pd.detail_value
            )
        ))
        AND (:brandList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:brandList, ',') s 
            WHERE LTRIM(RTRIM(s.value)) = b.brand_name
        ))
        AND ((:minPrice IS NULL OR :maxPrice IS NULL) 
             OR pv.price BETWEEN :minPrice AND :maxPrice)
        AND (:seasonList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:seasonList, ',') s 
            WHERE p.description LIKE '%' + LTRIM(RTRIM(s.value)) + '%'
        ))
    """,
            nativeQuery = true)
    Page<Product> filterOrderByMaxVariantPriceDesc(
            @Param("genderList") String genderList,
            @Param("brandList") String brandList,
            @Param("seasonList") String seasonList,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Query(value = """
    SELECT 
        p.product_id, p.brand_id, p.product_name, p.description, p.image_url,
        SUM(ISNULL(oi.quantity, 0)) AS total_quantity
    FROM Product p
    JOIN Brand b ON p.brand_id = b.brand_id
    LEFT JOIN ProductDetail pd ON p.product_id = pd.product_id
    LEFT JOIN ProductVariant pv ON p.product_id = pv.product_id
    LEFT JOIN OrderItem oi ON pv.product_variant_id = oi.product_variant_id
    WHERE 
        (:genderList IS NULL OR (
            pd.detail_name = 'suitable_gender' AND 
            EXISTS (
                SELECT 1 FROM STRING_SPLIT(:genderList, ',') s 
                WHERE LTRIM(RTRIM(s.value)) = pd.detail_value
            )
        ))
        AND (:brandList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:brandList, ',') s 
            WHERE LTRIM(RTRIM(s.value)) = b.brand_name
        ))
        AND ((:minPrice IS NULL OR :maxPrice IS NULL) 
             OR pv.price BETWEEN :minPrice AND :maxPrice)
        AND (:seasonList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:seasonList, ',') s 
            WHERE p.description LIKE '%' + LTRIM(RTRIM(s.value)) + '%'
        ))
    GROUP BY 
        p.product_id, p.brand_id, p.product_name, p.description, p.image_url
    ORDER BY 
        total_quantity DESC
    """,
            countQuery = """
    SELECT COUNT(DISTINCT p.product_id)
    FROM Product p
    JOIN Brand b ON p.brand_id = b.brand_id
    LEFT JOIN ProductDetail pd ON p.product_id = pd.product_id
    LEFT JOIN ProductVariant pv ON p.product_id = pv.product_id
    LEFT JOIN OrderItem oi ON pv.product_variant_id = oi.product_variant_id
    WHERE 
        (:genderList IS NULL OR (
            pd.detail_name = 'suitable_gender' AND 
            EXISTS (
                SELECT 1 FROM STRING_SPLIT(:genderList, ',') s 
                WHERE LTRIM(RTRIM(s.value)) = pd.detail_value
            )
        ))
        AND (:brandList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:brandList, ',') s 
            WHERE LTRIM(RTRIM(s.value)) = b.brand_name
        ))
        AND ((:minPrice IS NULL OR :maxPrice IS NULL) 
             OR pv.price BETWEEN :minPrice AND :maxPrice)
        AND (:seasonList IS NULL OR EXISTS (
            SELECT 1 FROM STRING_SPLIT(:seasonList, ',') s 
            WHERE p.description LIKE '%' + LTRIM(RTRIM(s.value)) + '%'
        ))
    """,
            nativeQuery = true)
    Page<Product> filterTopSellingProducts(
            @Param("genderList") String genderList,
            @Param("brandList") String brandList,
            @Param("seasonList") String seasonList,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query(value = """
    SELECT pv.product_variant_id, p.product_name, b.brand_name, pv.volume, pv.price, p.image_url, SUM(oi.quantity)
    FROM [OrderItem] oi
    JOIN [ProductVariant] pv ON pv.product_variant_id = oi.product_variant_id
    JOIN [Product] p ON p.product_id = pv.product_id
    JOIN [Brand] b ON b.brand_id = p.brand_id
    JOIN [Order] o ON o.order_id = oi.order_id
    LEFT JOIN [ProductDetail] pd ON p.product_id = pd.product_id AND pd.detail_name = 'suitable_gender'
    WHERE o.order_status = 'delivered'
      AND (:category IS NULL OR pd.detail_value = :category)
    GROUP BY pv.product_variant_id, p.product_name, b.brand_name, pv.volume, pv.price, p.image_url
    ORDER BY SUM(oi.quantity) DESC
    """, nativeQuery = true)
    List<Object[]> findTop10TopSellingProducts(@Param("category") String category);

    @Query(value = """
    SELECT pv.product_variant_id, p.product_name, b.brand_name, pv.volume, pv.price, 
           p.image_url, pv.quantity_in_stock, pv.reorder_level
    FROM [ProductVariant] pv
    JOIN [Product] p ON p.product_id = pv.product_id
    JOIN [Brand] b ON b.brand_id = p.brand_id
    WHERE pv.quantity_in_stock <= pv.reorder_level
      AND pv.reorder_level IS NOT NULL
      AND pv.reorder_level > 0
    ORDER BY 
      CASE 
        WHEN pv.reorder_level = 0 THEN 0 
        ELSE (pv.quantity_in_stock * 1.0 / pv.reorder_level) 
      END ASC
    OFFSET 0 ROWS
    FETCH NEXT :limit ROWS ONLY
    """, nativeQuery = true)
    List<Object[]> findProductsWithLowStock(@Param("limit") int limit);

    // Lọc sản phẩm admin
    @Query(value = """
    SELECT 
        p.product_id,
        p.brand_id,
        p.image_url,
        p.product_name,
        p.description,
        pv.volume,
        pv.price,
        pv.quantity_in_stock,
        pv.reorder_level,
        pv.discount_price,
        pd.detail_value AS suitable_gender
    FROM Product p
    JOIN ProductVariant pv ON p.product_id = pv.product_id
    LEFT JOIN ProductDetail pd 
        ON p.product_id = pd.product_id AND pd.detail_name = 'suitable_gender'
    WHERE 
        (:gender IS NULL OR pd.detail_value = :gender)
        AND (:keyword IS NULL OR p.product_name LIKE %:keyword%)
        AND (
            :stockStatus IS NULL 
            OR (:stockStatus = 'out_of_stock' AND pv.quantity_in_stock = 0)
            OR (:stockStatus = 'low_stock' AND pv.quantity_in_stock > 0 AND pv.quantity_in_stock <= pv.reorder_level)
        )
    ORDER BY
        CASE WHEN :sortBy = 'price' AND :sortDir = 'asc' THEN pv.price END ASC,
        CASE WHEN :sortBy = 'price' AND :sortDir = 'desc' THEN pv.price END DESC,
        CASE WHEN :sortBy = 'stock' AND :sortDir = 'asc' THEN pv.quantity_in_stock END ASC,
        CASE WHEN :sortBy = 'stock' AND :sortDir = 'desc' THEN pv.quantity_in_stock END DESC
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM Product p
    JOIN ProductVariant pv ON p.product_id = pv.product_id
    LEFT JOIN ProductDetail pd 
        ON p.product_id = pd.product_id AND pd.detail_name = 'suitable_gender'
    WHERE 
        (:gender IS NULL OR pd.detail_value = :gender)
        AND (:keyword IS NULL OR p.product_name LIKE %:keyword%)
        AND (
            :stockStatus IS NULL 
            OR (:stockStatus = 'out_of_stock' AND pv.quantity_in_stock = 0)
            OR (:stockStatus = 'low_stock' AND pv.quantity_in_stock > 0 AND pv.quantity_in_stock <= pv.reorder_level)
        )
    """,
            nativeQuery = true)
    Page<Object[]> findFilteredProductsWithSort(
            @Param("gender") String gender,                 // Men, Women, Unisex
            @Param("keyword") String keyword,               // Search keyword
            @Param("sortBy") String sortBy,                 // price, stock
            @Param("sortDir") String sortDir,               // asc, desc
            @Param("stockStatus") String stockStatus,       // out_of_stock, low_stock
            Pageable pageable
    );


}
