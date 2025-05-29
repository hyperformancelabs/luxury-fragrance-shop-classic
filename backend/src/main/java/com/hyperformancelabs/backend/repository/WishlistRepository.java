package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {

    // Tìm wishlist theo khách hàng
    List<Wishlist> findByCustomer_CustomerId(Integer customerId);

    // Xóa sản phẩm ra khỏi wishlist
    void deleteByCustomer_CustomerIdAndProductVariant_ProductVariantId(Integer customerId, Integer productVariantId);

    // Xóa toàn bộ wishlist
    void deleteByCustomer_CustomerId(Integer customerId);
}
