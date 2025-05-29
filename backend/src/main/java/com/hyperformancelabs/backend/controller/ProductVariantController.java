package com.hyperformancelabs.backend.controller;

import com.hyperformancelabs.backend.dto.ProductVariantDTO;
import com.hyperformancelabs.backend.service.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/product-variants")
public class ProductVariantController {

    @Autowired
    private ProductVariantService productVariantService;

    /**
     * Lấy danh sách biến thể của một sản phẩm
     * @param productId ID của sản phẩm
     * @return Danh sách biến thể của sản phẩm
     */
    @GetMapping("/product/{productId}")
    @ResponseBody
    public ResponseEntity<List<ProductVariantDTO>> getProductVariantsByProductId(@PathVariable Integer productId) {
        List<ProductVariantDTO> variants = productVariantService.getProductVariantsByProductId(productId);
        return ResponseEntity.ok(variants);
    }

    /**
     * Lấy thông tin chi tiết của một biến thể sản phẩm
     * @param variantId ID của biến thể sản phẩm
     * @return Thông tin chi tiết của biến thể sản phẩm
     */
    @GetMapping("/{variantId}")
    @ResponseBody
    public ResponseEntity<ProductVariantDTO> getProductVariantById(@PathVariable Integer variantId) {
        ProductVariantDTO variant = productVariantService.getProductVariantById(variantId);
        if (variant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(variant);
    }
}
