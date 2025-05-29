package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.FlashSaleProductDTO;
import com.hyperformancelabs.backend.dto.ProductAdminDisplayDTO;
import com.hyperformancelabs.backend.dto.ProductDTO;
import com.hyperformancelabs.backend.dto.TopSellingProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    // Lấy tất cả sản phẩm với phân trang
    Page<ProductDTO> getAllProducts(Pageable pageable);

    // Tìm sản phẩm theo tên thương hiệu với phân trang
    Page<ProductDTO> getProductsByBrandName(String brandName, Pageable pageable);

    // Tìm sản phẩm theo tên sản phẩm với phân trang
    Page<ProductDTO> getProductsByProductName(String productName, Pageable pageable);
    
    // Lấy sản phẩm theo ID
    ProductDTO getProductById(Integer productId);
    
    // Lấy sản phẩm liên quan (cùng thương hiệu)
    List<ProductDTO> getRelatedProducts(Integer productId, int limit);
    
    // Lấy top sản phẩm bán chạy
    List<ProductDTO> getTopSellingProducts(int limit);

    List<TopSellingProductDTO> getTopSellingProducts(String category, int limit);

    // Lấy danh sách sản phẩm trong flash sale
    List<FlashSaleProductDTO> getFlashSaleProducts();

    // Lọc sản phẩm
    Page<ProductDTO> filterProductsPaged(String genderList,
                                         String brandList,
                                         String seasonList,
                                         BigDecimal minPrice,
                                         BigDecimal maxPrice,
                                         Pageable pageable);

    Page<ProductDTO> getAllProductsOrderByMinVariantPriceAsc(String genderList,
                                                             String brandList,
                                                             String seasonList,
                                                             BigDecimal minPrice,
                                                             BigDecimal maxPrice,
                                                             Pageable pageable);

    Page<ProductDTO> getAllProductsOrderByMaxVariantPriceDesc(String genderList,
                                                              String brandList,
                                                              String seasonList,
                                                              BigDecimal minPrice,
                                                              BigDecimal maxPrice,
                                                              Pageable pageable);

    Page<ProductDTO> getAllProductsOrderByTopSelling(String genderList,
                                                      String brandList,
                                                      String seasonList,
                                                      BigDecimal minPrice,
                                                      BigDecimal maxPrice,
                                                      Pageable pageable);

    Page<ProductDTO> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);

    // --------------------------------------- ADMIN -----------------------------------------------------

    // Lọc sản phẩm
    Page<ProductAdminDisplayDTO> filterAdminProductsPaged(String category,
                                                     String keyword,
                                                     String sortBy,
                                                     String sortDir,
                                                     String stockStatus,
                                                     Pageable pageable);

    // Thêm sản phẩm
    ProductDTO addProduct(ProductDTO productDTO);

    // Cập nhật sản phẩm
    ProductDTO updateProduct(ProductDTO productDTO);

    // Xóa sản phẩm
    void deleteProduct(Integer productId);
}
