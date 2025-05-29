package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.ProductVariantDTO;
import com.hyperformancelabs.backend.model.ProductVariant;

import java.util.List;

public interface ProductVariantService {
    // Tìm tất cả biến thể của một sản phẩm
    List<ProductVariantDTO> getProductVariantsByProductId(Integer productId);

    // Tìm biến thể theo ID
    ProductVariantDTO getProductVariantById(Integer productVariantId);

    // Tìm biến thể đầu tiên của một sản phẩm
    ProductVariantDTO findFirstByProduct_ProductId(Integer productId);

    // Lấy giá min-max của tất cả biến thể
    List<Object[]> getMinAndMaxVariantPrice();

//    // Lấy danh sách biến thể của một sản phẩm
//    List<ProductVariantDTO> getProductVariantsByProductId(Integer productId);

//    // Lấy biến thể theo ID
//    ProductVariantDTO getProductVariantById(Integer productVariantId);

    // Tìm biến thể theo sản phẩm và dung tích
    ProductVariantDTO findByProduct_ProductIdAndVolume(Integer productId, Integer volume);

    // Thêm biến thể
    void addProductVariant(ProductVariantDTO productVariant);

    // Cập nhật biến thể
    void updateProductVariant(ProductVariantDTO productVariant);

    // Xóa biến thể
    void deleteProductVariant(Integer productVariantId);
}
