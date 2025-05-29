package com.hyperformancelabs.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperformancelabs.backend.dto.BrandDTO;
import com.hyperformancelabs.backend.dto.ProductDTO;
import com.hyperformancelabs.backend.dto.ProductVariantDTO;
import com.hyperformancelabs.backend.service.BrandService;
import com.hyperformancelabs.backend.service.ProductService;
import com.hyperformancelabs.backend.service.ProductVariantService;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private BrandService brandService;

    @GetMapping
    public String showProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String genders,
            @RequestParam(required = false) String brands,
            @RequestParam(required = false) String seasons,
            @RequestParam(required = false) BigDecimal min,
            @RequestParam(required = false) BigDecimal max,
            Model model,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page, size);

        // Lấy khoảng giá toàn bộ sản phẩm
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


        // Xử lý chuỗi rỗng thành null để tránh lỗi lọc
        if (genders != null && genders.trim().isEmpty()) genders = null;
        if (brands != null && brands.trim().isEmpty()) brands = null;
        if (seasons != null && seasons.trim().isEmpty()) seasons = null;

        // Xử lý min/max rỗng hoặc không hợp lệ
        if (min.compareTo(BigDecimal.ZERO) < 0) min = minPrice;
        if (max.compareTo(BigDecimal.ZERO) < 0) max = maxPrice;


        List<BrandDTO> brandDTOs = brandService.getAllBrands();

        Page<ProductDTO> productPage = productService.filterProductsPaged(
                genders, brands, seasons, min, max, pageable
        );

        List<ProductDTO> products = productPage.getContent();

        // Biến map dữ liệu sản phẩm
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

        model.addAttribute("currentUri", request.getRequestURI());

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

        return "shop/product-list";
    }

    @GetMapping("/sort/min-price")
    public String showProductListOrderByMinVariantPriceAsc(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String genders,
            @RequestParam(required = false) String brands,
            @RequestParam(required = false) String seasons,
            @RequestParam(required = false) BigDecimal min,
            @RequestParam(required = false) BigDecimal max,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);

        // Lấy khoảng giá toàn bộ sản phẩm
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


        // Xử lý chuỗi rỗng thành null để tránh lỗi lọc
        if (genders != null && genders.trim().isEmpty()) genders = null;
        if (brands != null && brands.trim().isEmpty()) brands = null;
        if (seasons != null && seasons.trim().isEmpty()) seasons = null;

        // Xử lý min/max rỗng hoặc không hợp lệ
        if (min.compareTo(BigDecimal.ZERO) < 0) min = minPrice;
        if (max.compareTo(BigDecimal.ZERO) < 0) max = maxPrice;


        List<BrandDTO> brandDTOs = brandService.getAllBrands();

        Page<ProductDTO> productPage = productService.getAllProductsOrderByMinVariantPriceAsc(
                genders, brands, seasons, min, max, pageable
        );

        List<ProductDTO> products = productPage.getContent();

        // Biến map dữ liệu sản phẩm
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
        model.addAttribute("currentSort", "min-price");

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

        return "shop/product-list";
    }

    @GetMapping("/sort/max-price")
    public String showProductListOrderByMaxVariantPriceDesc(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String genders,
            @RequestParam(required = false) String brands,
            @RequestParam(required = false) String seasons,
            @RequestParam(required = false) BigDecimal min,
            @RequestParam(required = false) BigDecimal max,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);

        // Lấy khoảng giá toàn bộ sản phẩm
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


        // Xử lý chuỗi rỗng thành null để tránh lỗi lọc
        if (genders != null && genders.trim().isEmpty()) genders = null;
        if (brands != null && brands.trim().isEmpty()) brands = null;
        if (seasons != null && seasons.trim().isEmpty()) seasons = null;

        // Xử lý min/max rỗng hoặc không hợp lệ
        if (min.compareTo(BigDecimal.ZERO) < 0) min = minPrice;
        if (max.compareTo(BigDecimal.ZERO) < 0) max = maxPrice;


        List<BrandDTO> brandDTOs = brandService.getAllBrands();

        Page<ProductDTO> productPage = productService.getAllProductsOrderByMaxVariantPriceDesc(
                genders, brands, seasons, min, max, pageable
        );

        List<ProductDTO> products = productPage.getContent();

        // Biến map dữ liệu sản phẩm
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
        model.addAttribute("currentSort", "max-price");

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

        return "shop/product-list";
    }

    @GetMapping("/sort/top-selling")
    public String showProductListOrderByTopSelling(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String genders,
            @RequestParam(required = false) String brands,
            @RequestParam(required = false) String seasons,
            @RequestParam(required = false) BigDecimal min,
            @RequestParam(required = false) BigDecimal max,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);

        // Lấy khoảng giá toàn bộ sản phẩm
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


        // Xử lý chuỗi rỗng thành null để tránh lỗi lọc
        if (genders != null && genders.trim().isEmpty()) genders = null;
        if (brands != null && brands.trim().isEmpty()) brands = null;
        if (seasons != null && seasons.trim().isEmpty()) seasons = null;

        // Xử lý min/max rỗng hoặc không hợp lệ
        if (min.compareTo(BigDecimal.ZERO) < 0) min = minPrice;
        if (max.compareTo(BigDecimal.ZERO) < 0) max = maxPrice;


        List<BrandDTO> brandDTOs = brandService.getAllBrands();

        Page<ProductDTO> productPage = productService.getAllProductsOrderByTopSelling(
                genders, brands, seasons, min, max, pageable
        );

        List<ProductDTO> products = productPage.getContent();

        // Biến map dữ liệu sản phẩm
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
        model.addAttribute("currentSort", "top-selling");

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

        return "shop/product-list";
    }

    @GetMapping("/product/{id}")
    public String showProductDetail(@PathVariable Integer id, Model model) {
        ProductDTO product = productService.getProductById(id);
        if (product == null) {
            return "error/404";
        }

        List<ProductDTO> relatedProducts = productService.getRelatedProducts(id, 4);

        // Lấy danh sách biến thể sản phẩm (kích thước, giá)
        List<ProductVariantDTO> productVariants = productVariantService.getProductVariantsByProductId(id);

        // Nếu không có biến thể nào, tạo dữ liệu mẫu
//        if (productVariants.isEmpty()) {
//            productVariants = new ArrayList<>();
//            productVariants.add(new ProductVariantDTO(1, id, 50, new BigDecimal(500000)));
//            productVariants.add(new ProductVariantDTO(2, id, 75, new BigDecimal(1250000)));
//            productVariants.add(new ProductVariantDTO(3, id, 100, new BigDecimal(2500000)));
//        }

        // Lấy giá của biến thể đầu tiên làm giá mặc định
        BigDecimal defaultPrice = productVariants.get(0).getPrice();
        BigDecimal originalPrice = defaultPrice.multiply(new BigDecimal("1.2")).setScale(0, RoundingMode.HALF_UP);

        // Thêm dữ liệu vào model
        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("productVariants", productVariants);
        model.addAttribute("rating", 4.5);
        model.addAttribute("reviewCount", 125);
        model.addAttribute("availability", "Còn hàng");
        model.addAttribute("originalPrice", originalPrice);
        model.addAttribute("salePrice", defaultPrice);

        // Thêm dữ liệu mẫu cho các hình ảnh sản phẩm
        List<String> images = new ArrayList<>();
        images.add(product.getImageUrl());
        images.add(product.getImageUrl()); // Trong thực tế, bạn sẽ có nhiều hình ảnh khác nhau
        images.add(product.getImageUrl());
        model.addAttribute("images", images);

        // Thêm dữ liệu mẫu cho các khuyến nghị theo mùa
        Map<String, Integer> seasonalRecommendations = new HashMap<>();
        seasonalRecommendations.put("MÙA ĐÔNG", 30);
        seasonalRecommendations.put("MÙA XUÂN", 70);
        seasonalRecommendations.put("MÙA HẠ", 90);
        seasonalRecommendations.put("MÙA THU", 60);
        seasonalRecommendations.put("BAN NGÀY", 80);
        seasonalRecommendations.put("BAN ĐÊM", 50);
        model.addAttribute("seasonalRecommendations", seasonalRecommendations);

        return "shop/product-detail";
    }


    private String convertVariantsToJson(List<ProductVariantDTO> variants) {
        if (variants == null || variants.isEmpty()) {
            return "[]";
        }

        try {
            // Chuyển đổi danh sách biến thể thành danh sách các map để dễ dàng hơn trong JSON
            List<Map<String, Object>> variantsList = new ArrayList<>();
            for (ProductVariantDTO variant : variants) {
                Map<String, Object> variantMap = new HashMap<>();
                variantMap.put("id", variant.getProductVariantId());
                variantMap.put("volume", variant.getVolume());
                variantMap.put("price", variant.getPrice());
                variantMap.put("discountPrice", variant.getDiscountPrice());
                variantMap.put("stock", variant.getQuantityInStock());
                variantsList.add(variantMap);
            }

            // Chuyển đổi danh sách thành chuỗi JSON
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(variantsList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "[]";
        }
    }
}
