package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.ProductDetailDTO;
import com.hyperformancelabs.backend.model.ProductDetail;
import com.hyperformancelabs.backend.repository.ProductDetailRepository;
import com.hyperformancelabs.backend.repository.ProductRepository;
import com.hyperformancelabs.backend.service.ProductDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductDetailServiceImpl implements ProductDetailService {
    @Autowired
    private ProductDetailRepository productDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public ProductDetailDTO findByProductIdAndDetailName(Integer productId, String detailName) {
        ProductDetail entity = productDetailRepository.findByProduct_ProductIdAndDetailName(productId, detailName);
        return entity != null ? convertToProductDetailDTO(entity) : null;
    }

    @Override
    public void addProductDetail(ProductDetailDTO productDetailDTO) {
        ProductDetail productDetail = new ProductDetail();
        productDetail.setProduct(productRepository.findById(productDetailDTO.getProductId()).orElse(null));
        productDetail.setDetailName(productDetailDTO.getDetailName());
        productDetail.setDetailValue(productDetailDTO.getDetailValue());
        productDetail.setNote(productDetailDTO.getNote());
        productDetailRepository.save(productDetail);
    }

    @Override
    public void updateProductDetail(ProductDetailDTO productDetailDTO) {
        ProductDetail productDetail = productDetailRepository.findById(productDetailDTO.getProductDetailId()).orElse(null);
        if (productDetail == null) {
            return;
        }
        productDetail.setProduct(productRepository.findById(productDetailDTO.getProductId()).orElse(null));
        productDetail.setDetailName(productDetailDTO.getDetailName());
        productDetail.setDetailValue(productDetailDTO.getDetailValue());
        productDetail.setNote(productDetailDTO.getNote());
        productDetailRepository.save(productDetail);
    }

    @Override
    public void deleteProductDetail(Integer productDetailId) {
        productDetailRepository.deleteById(productDetailId);
    }

    @Override
    public void saveOrUpdateProductDetail(ProductDetailDTO dto) {
        ProductDetail existing = productDetailRepository
                .findByProduct_ProductIdAndDetailName(dto.getProductId(), dto.getDetailName());

        if (existing == null) {
            existing = new ProductDetail();
            existing.setProduct(productRepository.findById(dto.getProductId()).orElseThrow());
            existing.setDetailName(dto.getDetailName());
        }

        existing.setDetailValue(dto.getDetailValue());
        existing.setNote(dto.getNote());
        productDetailRepository.save(existing);
    }

    private ProductDetailDTO convertToProductDetailDTO(ProductDetail productDetail) {
        return new ProductDetailDTO(
                productDetail.getProductDetailId(),
                productDetail.getProduct().getProductId(),
                productDetail.getDetailName(),
                productDetail.getDetailValue(),
                productDetail.getNote()
        );
    }
}
