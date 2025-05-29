package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {
    
    // Tìm tất cả biến thể của một sản phẩm
    List<ProductVariant> findByProduct_ProductId(Integer productId);

    // Tìm biến thể theo ID
    ProductVariant findByProductVariantId(Integer productVariantId);

    // Tìm biến thể đầu tiên của một sản phẩm
    @Query("SELECT pv FROM ProductVariant pv " +
           "WHERE pv.product.productId = :productId " +
           "ORDER BY pv.productVariantId ASC")
    Optional<ProductVariant> findFirstByProduct_ProductId(@Param("productId") Integer productId);
    
    // Tìm biến thể theo sản phẩm và dung tích
    Optional<ProductVariant> findByProduct_ProductIdAndVolume(Integer productId, Integer volume);
    
    // Tìm biến thể với eager loading thông tin sản phẩm
    @Query("SELECT pv FROM ProductVariant pv " +
           "LEFT JOIN FETCH pv.product p " +
           "LEFT JOIN FETCH p.brand " +
           "WHERE pv.productVariantId = :variantId")
    Optional<ProductVariant> findByIdWithProductDetails(@Param("variantId") Integer variantId);
    
    // Tìm biến thể có số lượng dưới mức tái đặt hàng
    @Query("SELECT pv FROM ProductVariant pv " +
           "WHERE pv.quantityInStock <= pv.reorderLevel " +
           "AND pv.reorderLevel > 0")
    List<ProductVariant> findVariantsNeedingRestock();
    
    // Tìm biến thể theo khoảng giá
    @Query("SELECT pv FROM ProductVariant pv " +
           "WHERE pv.price BETWEEN :minPrice AND :maxPrice")
    List<ProductVariant> findByPriceBetween(
            @Param("minPrice") java.math.BigDecimal minPrice, 
            @Param("maxPrice") java.math.BigDecimal maxPrice);

    // Tìm giá min - max của tất cả biến thể
    @Query("SELECT MIN(pv.price), MAX(pv.price) FROM ProductVariant pv")
    List<Object[]> findMinAndMaxVariantPrice();
}
