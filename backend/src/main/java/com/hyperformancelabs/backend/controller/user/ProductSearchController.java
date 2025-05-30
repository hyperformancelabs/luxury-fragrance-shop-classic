package com.hyperformancelabs.backend.controller.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperformancelabs.backend.dto.common.response.BrandDTO;
import com.hyperformancelabs.backend.dto.common.response.ProductDTO;
import com.hyperformancelabs.backend.dto.common.response.ProductVariantDTO;
import com.hyperformancelabs.backend.service.BrandService;
import com.hyperformancelabs.backend.service.ProductService;
import com.hyperformancelabs.backend.service.ProductVariantService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/search")
public class ProductSearchController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private BrandService brandService;

    @GetMapping
    public String searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String genders,
            @RequestParam(required = false) String brands,
            @RequestParam(required = false) String seasons,
            @RequestParam(required = false) BigDecimal min,
            @RequestParam(required = false) BigDecimal max,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            Model model,
            HttpServletRequest request) {

        // Validate and clean parameters
        if (q == null || q.trim().isEmpty()) {
            // Redirect to shop page if no search query
            return "redirect:/shop";
        }

        String keyword = q.trim();
        Pageable pageable = PageRequest.of(page, size);

        // Get min/max price range from all products
        List<Object[]> resultList = productVariantService.getMinAndMaxVariantPrice();
        BigDecimal minPrice = BigDecimal.ZERO;
        BigDecimal maxPrice = BigDecimal.ZERO;

        if (!resultList.isEmpty()) {
            Object[] result = resultList.get(0);
            if (result[0] instanceof BigDecimal) minPrice = (BigDecimal) result[0];
            if (result[1] instanceof BigDecimal) maxPrice = (BigDecimal) result[1];
        }

        if (min == null) min = minPrice;
        if (max == null) max = maxPrice;

        // Clean empty strings to null to avoid filter errors
        if (genders != null && genders.trim().isEmpty()) genders = null;
        if (brands != null && brands.trim().isEmpty()) brands = null;
        if (seasons != null && seasons.trim().isEmpty()) seasons = null;

        // Validate min/max prices
        if (min.compareTo(BigDecimal.ZERO) < 0) min = minPrice;
        if (max.compareTo(BigDecimal.ZERO) < 0) max = maxPrice;

        // Get brands for filter
        List<BrandDTO> brandDTOs = brandService.getAllBrands();

        // Search products
        Page<ProductDTO> productPage = productService.searchProducts(
                keyword, genders, brands, seasons, min, max, sortBy, sortDir, pageable
        );

        List<ProductDTO> products = productPage.getContent();

        // Process product data for display
        Map<Integer, List<ProductVariantDTO>> productVariantMap = products.stream()
                .map(product -> productVariantService.getProductVariantsByProductId(product.getProductId()))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(ProductVariantDTO::getProductId));

        Map<Integer, BigDecimal[]> productPriceRangeMap = new HashMap<>();
        Map<Integer, String> productVariantsMap = new HashMap<>();
        Map<Integer, Integer> productFirstVariantMap = new HashMap<>();
        Map<Integer, Boolean> productHasStockMap = new HashMap<>();

        for (Map.Entry<Integer, List<ProductVariantDTO>> entry : productVariantMap.entrySet()) {
            Integer productId = entry.getKey();
            List<ProductVariantDTO> variants = entry.getValue();

            if (!variants.isEmpty()) {
                Optional<BigDecimal> minVal = variants.stream().map(ProductVariantDTO::getPrice).min(Comparator.naturalOrder());
                Optional<BigDecimal> maxVal = variants.stream().map(ProductVariantDTO::getPrice).max(Comparator.naturalOrder());

                minVal.ifPresent(bigDecimal -> productPriceRangeMap.put(productId, new BigDecimal[]{bigDecimal, maxVal.get()}));

                productVariantsMap.put(productId, convertVariantsToJson(variants));

                Optional<ProductVariantDTO> firstInStock = variants.stream()
                        .filter(v -> v.getQuantityInStock() > 0)
                        .findFirst();

                productFirstVariantMap.put(productId, firstInStock.map(ProductVariantDTO::getProductVariantId).orElse(null));
                productHasStockMap.put(productId, firstInStock.isPresent());
            }
        }

        // Add attributes to model
        model.addAttribute("currentUri", request.getRequestURI());
        model.addAttribute("searchQuery", keyword);

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());

        model.addAttribute("productPriceRangeMap", productPriceRangeMap);
        model.addAttribute("productVariantsMap", productVariantsMap);
        model.addAttribute("productFirstVariantMap", productFirstVariantMap);
        model.addAttribute("productHasStockMap", productHasStockMap);

        model.addAttribute("brands", brandDTOs);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        // Search specific attributes
        model.addAttribute("currentBrand", brands);
        model.addAttribute("currentSearch", keyword);
        model.addAttribute("currentGender", genders);
        model.addAttribute("currentSeasons", seasons);
        model.addAttribute("currentMin", min);
        model.addAttribute("currentMax", max);
        model.addAttribute("currentSort", sortBy + "-" + sortDir);

        return "user/shop/product-list";
    }

    /**
     * API endpoint for real-time search suggestions
     */
    @GetMapping("/api/suggestions")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSearchSuggestions(
            @RequestParam String q,
            @RequestParam(defaultValue = "8") int limit) {

        Map<String, Object> response = new HashMap<>();

        if (q == null || q.trim().isEmpty() || q.trim().length() < 2) {
            response.put("success", false);
            response.put("message", "Từ khóa tìm kiếm phải có ít nhất 2 ký tự");
            response.put("suggestions", Collections.emptyList());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String keyword = q.trim();
            List<ProductDTO> suggestions = productService.searchProductSuggestions(keyword, limit);

            // Transform to simple response format
            List<Map<String, Object>> suggestionData = suggestions.stream().map(product -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", product.getProductId());
                item.put("name", product.getProductName());
                item.put("brand", product.getBrandName());
                item.put("image", product.getImageUrl());
                item.put("url", "/shop/product/" + product.getProductId());
                return item;
            }).collect(Collectors.toList());

            response.put("success", true);
            response.put("keyword", keyword);
            response.put("suggestions", suggestionData);
            response.put("total", suggestionData.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi tìm kiếm: " + e.getMessage());
            response.put("suggestions", Collections.emptyList());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * API endpoint for full search results (JSON)
     */
    @GetMapping("/api/products")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchProductsApi(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String genders,
            @RequestParam(required = false) String brands,
            @RequestParam(required = false) String seasons,
            @RequestParam(required = false) BigDecimal min,
            @RequestParam(required = false) BigDecimal max,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {

        Map<String, Object> response = new HashMap<>();

        if (q == null || q.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Vui lòng nhập từ khóa tìm kiếm");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String keyword = q.trim();
            Pageable pageable = PageRequest.of(page, size);

            // Get min/max price range
            List<Object[]> resultList = productVariantService.getMinAndMaxVariantPrice();
            BigDecimal minPrice = BigDecimal.ZERO;
            BigDecimal maxPrice = BigDecimal.ZERO;

            if (!resultList.isEmpty()) {
                Object[] result = resultList.get(0);
                if (result[0] instanceof BigDecimal) minPrice = (BigDecimal) result[0];
                if (result[1] instanceof BigDecimal) maxPrice = (BigDecimal) result[1];
            }

            if (min == null) min = minPrice;
            if (max == null) max = maxPrice;

            // Clean parameters
            if (genders != null && genders.trim().isEmpty()) genders = null;
            if (brands != null && brands.trim().isEmpty()) brands = null;
            if (seasons != null && seasons.trim().isEmpty()) seasons = null;

            // Search products
            Page<ProductDTO> productPage = productService.searchProducts(
                    keyword, genders, brands, seasons, min, max, sortBy, sortDir, pageable
            );

            // Transform products to API format
            List<Map<String, Object>> productData = productPage.getContent().stream().map(product -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", product.getProductId());
                item.put("name", product.getProductName());
                item.put("brand", product.getBrandName());
                item.put("description", product.getDescription());
                item.put("image", product.getImageUrl());
                item.put("url", "/shop/product/" + product.getProductId());

                // Get price range for this product
                List<ProductVariantDTO> variants = productVariantService.getProductVariantsByProductId(product.getProductId());
                if (!variants.isEmpty()) {
                    BigDecimal minVariantPrice = variants.stream()
                            .map(ProductVariantDTO::getPrice)
                            .min(Comparator.naturalOrder())
                            .orElse(BigDecimal.ZERO);
                    BigDecimal maxVariantPrice = variants.stream()
                            .map(ProductVariantDTO::getPrice)
                            .max(Comparator.naturalOrder())
                            .orElse(BigDecimal.ZERO);
                    
                    item.put("minPrice", minVariantPrice);
                    item.put("maxPrice", maxVariantPrice);
                    item.put("hasStock", variants.stream().anyMatch(v -> v.getQuantityInStock() > 0));
                }

                return item;
            }).collect(Collectors.toList());

            response.put("success", true);
            response.put("keyword", keyword);
            response.put("products", productData);
            response.put("pagination", Map.of(
                    "currentPage", page,
                    "totalPages", productPage.getTotalPages(),
                    "totalElements", productPage.getTotalElements(),
                    "size", size
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi tìm kiếm: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Helper method to convert variants to JSON string
     */
    private String convertVariantsToJson(List<ProductVariantDTO> variants) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(variants);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
} 