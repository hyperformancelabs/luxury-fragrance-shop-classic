package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.model.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Integer> {
    List<ProductDetail> findByProduct_ProductId(Integer productId);

    // Tìm chi tiết sản phẩm theo id sản phẩm và tên chi tiết
    ProductDetail findByProduct_ProductIdAndDetailName(Integer productId, String detailName);
}
