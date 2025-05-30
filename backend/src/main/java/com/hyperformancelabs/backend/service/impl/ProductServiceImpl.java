package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.common.response.FlashSaleProductDTO;
import com.hyperformancelabs.backend.dto.admin.response.ProductAdminDisplayDTO;
import com.hyperformancelabs.backend.dto.common.response.ProductDTO;
import com.hyperformancelabs.backend.dto.admin.response.TopSellingProductDTO;
import com.hyperformancelabs.backend.model.Product;
import com.hyperformancelabs.backend.repository.BrandRepository;
import com.hyperformancelabs.backend.repository.ProductRepository;
import com.hyperformancelabs.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Override
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::convertToProductDTO);
    }

    @Override
    public Page<ProductDTO> getProductsByBrandName(String brandName, Pageable pageable) {
        return productRepository.findByBrand_BrandName(brandName, pageable).map(this::convertToProductDTO);
    }

    @Override
    public Page<ProductDTO> getProductsByProductName(String productName, Pageable pageable) {
        return productRepository.findByProductNameContainingIgnoreCase(productName, pageable).map(this::convertToProductDTO);
    }
    
    @Override
    public ProductDTO getProductById(Integer productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        return productOptional.map(this::convertToProductDTO).orElse(null);
    }
    
    @Override
    public List<ProductDTO> getRelatedProducts(Integer productId, int limit) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return List.of();
        }
        
        // Lấy sản phẩm cùng thương hiệu, ngoại trừ sản phẩm hiện tại
        return productRepository.findByBrand_BrandIdAndProductIdNot(
                product.getBrand().getBrandId(), 
                productId, 
                PageRequest.of(0, limit)
            ).stream()
            .map(this::convertToProductDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<FlashSaleProductDTO> getFlashSaleProducts() {
        return productRepository.findActiveFlashSaleProducts()
                .stream()
                .map(this::mapToFlashSaleProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductDTO> filterProductsPaged(String genderList,
                                                String brandList,
                                                String seasonList,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                Pageable pageable){
        return productRepository.findFilteredProducts(genderList, brandList, seasonList, minPrice, maxPrice, pageable).map(this::convertToProductDTO);
    }

    @Override
    public Page<ProductDTO> getAllProductsOrderByMinVariantPriceAsc(String genderList,
                                                                    String brandList,
                                                                    String seasonList,
                                                                    BigDecimal minPrice,
                                                                    BigDecimal maxPrice,
                                                                    Pageable pageable) {
        return productRepository.filterOrderByMinVariantPriceAsc(genderList, brandList, seasonList, minPrice, maxPrice, pageable).map(this::convertToProductDTO);
    }

    @Override
    public Page<ProductDTO> getAllProductsOrderByMaxVariantPriceDesc(String genderList,
                                                                     String brandList,
                                                                     String seasonList,
                                                                     BigDecimal minPrice,
                                                                     BigDecimal maxPrice,
                                                                     Pageable pageable) {
        return productRepository.filterOrderByMaxVariantPriceDesc(genderList, brandList, seasonList, minPrice, maxPrice, pageable).map(this::convertToProductDTO);
    }

    @Override
    public Page<ProductDTO> findByProductNameContainingIgnoreCase(String productName, Pageable pageable) {
        return productRepository.findByProductNameContainingIgnoreCase(productName, pageable).map(this::convertToProductDTO);
    }

    @Override
    public Page<ProductAdminDisplayDTO> filterAdminProductsPaged(String category,
                                                                 String keyword,
                                                                 String sortBy,
                                                                 String sortDir,
                                                                 String stockStatus,
                                                                 Pageable pageable) {
        return productRepository.findFilteredProductsWithSort(category, keyword, sortBy, sortDir, stockStatus, pageable).map(this::convertToProductAdminDisplayDTO);
    }

    @Override
    public Page<ProductDTO> searchProducts(String keyword, 
                                          String genderList,
                                          String brandList, 
                                          String seasonList,
                                          BigDecimal minPrice,
                                          BigDecimal maxPrice,
                                          String sortBy,
                                          String sortDir,
                                          Pageable pageable) {
        return productRepository.searchProductsAdvanced(
                keyword, genderList, brandList, seasonList, 
                minPrice, maxPrice, sortBy, sortDir, pageable
        ).map(this::convertToProductDTO);
    }

    @Override
    public List<ProductDTO> searchProductSuggestions(String keyword, int limit) {
        return productRepository.findProductSuggestions(keyword, limit)
                .stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO addProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setProductName(productDTO.getProductName());
        product.setBrand(brandRepository.findByBrandName(productDTO.getBrandName()).orElse(null));
        product.setDescription(productDTO.getDescription());
        product.setImageUrl(productDTO.getImageUrl());
        productRepository.save(product);
        return convertToProductDTO(product);
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO) {
        Product product = productRepository.findById(productDTO.getProductId()).orElse(null);
        if (product == null) {
            return null;
        }
        product.setProductName(productDTO.getProductName());
        product.setBrand(brandRepository.findByBrandName(productDTO.getBrandName()).orElse(null));
        product.setDescription(productDTO.getDescription());
        product.setImageUrl(productDTO.getImageUrl());
        productRepository.save(product);
        return convertToProductDTO(product);
    }

    @Override
    public void deleteProduct(Integer productId) {
        productRepository.deleteById(productId);
    }

    private ProductAdminDisplayDTO convertToProductAdminDisplayDTO(Object[] result) {
        return new ProductAdminDisplayDTO(
                (Integer) result[0],
                (Integer) result[1],
                (String) result[2],
                (String) result[3],
                (String) result[4],
                (Integer) result[5],
                (BigDecimal) result[6],
                (Integer) result[7],
                (Integer) result[8],
                (BigDecimal) result[9],
                (String) result[10]
        );
    }

    private ProductDTO convertToProductDTO(Product product) {
        return new ProductDTO(
                product.getProductId(),
                product.getBrand().getBrandName(),
                product.getProductName(),
                product.getDescription(),
                product.getImageUrl(),
                null, // averageRating
                null  // totalReviews
        );
    }

    private FlashSaleProductDTO mapToFlashSaleProductDTO(Object[] product) {
        Object startDateObj = product[9];
        Object endDateObj = product[10];

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (startDateObj instanceof java.sql.Timestamp) {
            startDate = ((java.sql.Timestamp) startDateObj).toLocalDateTime();
        } else if (startDateObj instanceof java.sql.Date) {
            startDate = ((java.sql.Date) startDateObj).toLocalDate().atStartOfDay();
        }

        if (endDateObj instanceof java.sql.Timestamp) {
            endDate = ((java.sql.Timestamp) endDateObj).toLocalDateTime();
        } else if (endDateObj instanceof java.sql.Date) {
            endDate = ((java.sql.Date) endDateObj).toLocalDate().atStartOfDay();
        }

        return new FlashSaleProductDTO(
                (Integer) product[0],
                (String) product[1],
                (Integer) product[2],
                (BigDecimal) product[3],
                (String) product[4],
                (String) product[5],
                (String) product[6],
                (BigDecimal) product[7],
                (Integer) product[8],
                startDate,
                endDate
        );
    }
}