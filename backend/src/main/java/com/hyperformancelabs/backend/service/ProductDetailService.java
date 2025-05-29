package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.ProductDetailDTO;

public interface ProductDetailService {

    // Tìm chi tiết sản phẩm theo id sản phẩm và giá trị chi tiết
    ProductDetailDTO findByProductIdAndDetailName(Integer productId, String detailName);

    // Thêm chi tiết sản phẩm
    void addProductDetail(ProductDetailDTO productDetailDTO);

    // Cập nhật chi tiết sản phẩm
    void updateProductDetail(ProductDetailDTO productDetailDTO);

    // Xóa chi tiết sản phẩm
    void deleteProductDetail(Integer productDetailId);

    void saveOrUpdateProductDetail(ProductDetailDTO productDetailDTO);
}
