package com.hyperformancelabs.backend.controller;

import com.hyperformancelabs.backend.dto.*;
import com.hyperformancelabs.backend.model.ProductVariant;
import com.hyperformancelabs.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private InventoryTransactionService inventoryTransactionService;

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private WishlistService wishlistService;

    @GetMapping("/")
    public String home(Model model) {
        String username = getCurrentUsername();
        Set<Integer> wishlistProductIds = new HashSet<>();

        if (username != null) {
            List<WishlistDTO> wishlistItems = wishlistService.getWishlistItems(username);
            for (WishlistDTO item : wishlistItems) {
                ProductVariantDTO variant = productVariantService.getProductVariantById(item.getProductVariantId());
                if (variant != null) {
                    wishlistProductIds.add(variant.getProductId());
                }
            }
        }

        // ------------------- Get flash deal products ----------------------------------------
        List<ProductDTO> flashSaleProducts = new ArrayList<>();
        List<FlashSaleProductDTO> flashDealProducts = productService.getFlashSaleProducts();
        for (FlashSaleProductDTO flashDealProduct : flashDealProducts) {
            flashSaleProducts.add(productService.getProductById(flashDealProduct.getProductId()));
        }

        Map<Integer, List<ProductVariantDTO>> flashSaleProductVariantMap = flashDealProducts.stream()
                .map(flashDealProduct -> productVariantService.getProductVariantsByProductId(flashDealProduct.getProductId()))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(ProductVariantDTO::getProductId));

        Map<Integer, BigDecimal[]> flashSaleProductPriceRangeMap = new HashMap<>();
        Map<Integer, String> flashSaleProductVariants = new HashMap<>();
        Map<Integer, Boolean> flashSaleProductHasStockMap = new HashMap<>();
        Map<Integer, Integer> flashSaleFirstVariantMap = new HashMap<>();
        Map<Integer, Boolean> flashSaleProductInWishlistMap = new HashMap<>();

        for (Map.Entry<Integer, List<ProductVariantDTO>> entry : flashSaleProductVariantMap.entrySet()) {
            Integer productId = entry.getKey();
            List<ProductVariantDTO> variants = entry.getValue();

            if (!variants.isEmpty()) {
                Optional<BigDecimal> min = variants.stream().map(ProductVariantDTO::getPrice).min(Comparator.naturalOrder());
                Optional<BigDecimal> max = variants.stream().map(ProductVariantDTO::getPrice).max(Comparator.naturalOrder());
                min.ifPresent(bigDecimal -> flashSaleProductPriceRangeMap.put(productId, new BigDecimal[]{bigDecimal, max.get()}));

                flashSaleProductVariants.put(productId, convertVariantsToJson(variants));

                Optional<ProductVariantDTO> firstInStock = variants.stream().filter(v -> v.getQuantityInStock() > 0).findFirst();
                flashSaleFirstVariantMap.put(productId, firstInStock.map(ProductVariantDTO::getProductVariantId).orElse(null));

                flashSaleProductHasStockMap.put(productId, firstInStock.isPresent());

                flashSaleProductInWishlistMap.put(productId, wishlistProductIds.contains(productId));
            }
        }

        model.addAttribute("flashDealProducts", flashSaleProducts);
        model.addAttribute("flashSaleProductPriceRangeMap", flashSaleProductPriceRangeMap);
        model.addAttribute("flashSaleProductVariants", flashSaleProductVariants);
        model.addAttribute("flashSaleProductHasStockMap", flashSaleProductHasStockMap);
        model.addAttribute("flashSaleFirstVariantMap", flashSaleFirstVariantMap);
        model.addAttribute("flashSaleProductInWishlistMap", flashSaleProductInWishlistMap);


        // ---------------------------------- Get new products -------------------------------------------------------------------
        List<InventoryTransactionDTO> inventoryTransactions = inventoryTransactionService.findTop6ImportTransactionsNative();

        Map<Integer, List<ProductVariantDTO>> newProductVariantMap = inventoryTransactions.stream()
                .map(inventoryTransaction -> productVariantService.getProductVariantById(inventoryTransaction.getProductVariantId()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(ProductVariantDTO::getProductId));

        List<ProductDTO> newProducts = newProductVariantMap.keySet().stream()
                .map(productService::getProductById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Integer, BigDecimal[]> newProductPriceRangeMap = new HashMap<>();
        Map<Integer, String> newProductVariants = new HashMap<>();
        Map<Integer, Integer> newFirstVariantMap = new HashMap<>();
        Map<Integer, Boolean> newProductHasStockMap = new HashMap<>();
        Map<Integer, Boolean> newProductInWishlistMap = new HashMap<>();

        for (Map.Entry<Integer, List<ProductVariantDTO>> entry : newProductVariantMap.entrySet()) {
            Integer productId = entry.getKey();
            List<ProductVariantDTO> variants = entry.getValue();

            if (variants != null && !variants.isEmpty()) {
                // Giá min - max
                Optional<BigDecimal> min = variants.stream().map(ProductVariantDTO::getPrice).min(Comparator.naturalOrder());
                Optional<BigDecimal> max = variants.stream().map(ProductVariantDTO::getPrice).max(Comparator.naturalOrder());

                min.ifPresent(bigDecimal -> newProductPriceRangeMap.put(productId, new BigDecimal[]{bigDecimal, max.get()}));

                // JSON biến thể
                String variantsJson = convertVariantsToJson(variants);
                newProductVariants.put(productId, variantsJson);

                // Biến thể đầu tiên còn hàng
                Optional<ProductVariantDTO> firstInStock = variants.stream()
                        .filter(v -> v.getQuantityInStock() > 0)
                        .findFirst();

                newFirstVariantMap.put(productId, firstInStock.map(ProductVariantDTO::getProductVariantId).orElse(null));

                // Cờ còn hàng
                newProductHasStockMap.put(productId, firstInStock.isPresent());

                newProductInWishlistMap.put(productId, wishlistProductIds.contains(productId));
            }
        }

        model.addAttribute("newProducts", newProducts);
        model.addAttribute("newProductPriceRangeMap", newProductPriceRangeMap);
        model.addAttribute("newProductVariants", newProductVariants);
        model.addAttribute("newFirstVariantMap", newFirstVariantMap);
        model.addAttribute("newProductHasStockMap", newProductHasStockMap);
        model.addAttribute("newProductInWishlistMap", newProductInWishlistMap);


        // --------------------------------------------- Get best selling ----------------------------------------------------
        List<ProductDTO> bestSellingProducts = productService.getTopSellingProducts(6);

        Map<Integer, List<ProductVariantDTO>> bestSellingProductVariantMap = bestSellingProducts.stream()
                .map(product -> productVariantService.getProductVariantsByProductId(product.getProductId()))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(ProductVariantDTO::getProductId));

        Map<Integer, BigDecimal[]> bestSellingProductPriceRangeMap = new HashMap<>();
        Map<Integer, String> bestSellingProductVariants = new HashMap<>();
        Map<Integer, Integer> bestSellingFirstVariantMap = new HashMap<>();
        Map<Integer, Boolean> bestSellingProductHasStockMap = new HashMap<>();
        Map<Integer, Boolean> bestSellingProductInWishlistMap = new HashMap<>();

        for (Map.Entry<Integer, List<ProductVariantDTO>> entry : bestSellingProductVariantMap.entrySet()) {
            Integer productId = entry.getKey();
            List<ProductVariantDTO> variants = entry.getValue();

            if (variants != null && !variants.isEmpty()) {
                // Tính giá min-max
                Optional<BigDecimal> min = variants.stream().map(ProductVariantDTO::getPrice).min(Comparator.naturalOrder());
                Optional<BigDecimal> max = variants.stream().map(ProductVariantDTO::getPrice).max(Comparator.naturalOrder());

                min.ifPresent(minPrice -> bestSellingProductPriceRangeMap.put(productId, new BigDecimal[]{minPrice, max.get()}));

                // Convert variants to JSON string
                String variantsJson = convertVariantsToJson(variants);
                bestSellingProductVariants.put(productId, variantsJson);

                // Tìm biến thể đầu tiên còn hàng
                Optional<ProductVariantDTO> firstInStock = variants.stream()
                        .filter(v -> v.getQuantityInStock() > 0)
                        .findFirst();

                bestSellingFirstVariantMap.put(productId, firstInStock.map(ProductVariantDTO::getProductVariantId).orElse(null));

                // Cờ còn hàng
                bestSellingProductHasStockMap.put(productId, firstInStock.isPresent());

                bestSellingProductInWishlistMap.put(productId, wishlistProductIds.contains(productId));
            }
        }

        model.addAttribute("bestSellingProducts", bestSellingProducts);
        model.addAttribute("bestSellingProductPriceRangeMap", bestSellingProductPriceRangeMap);
        model.addAttribute("bestSellingProductVariants", bestSellingProductVariants);
        model.addAttribute("bestSellingFirstVariantMap", bestSellingFirstVariantMap);
        model.addAttribute("bestSellingProductHasStockMap", bestSellingProductHasStockMap);
        model.addAttribute("bestSellingProductInWishlistMap", bestSellingProductInWishlistMap);



        // --------------------------------------------- Add brand data - Lấy 12 brand đầu tiên ------------------------------------
        List<BrandDTO> allBrands = brandService.getAllBrands();
        List<BrandDTO> brands = allBrands.stream()
                .limit(12)
                .collect(Collectors.toList());
        model.addAttribute("brands", brands);
//        model.addAttribute("brands", List.of(
//            "Chanel", "Dior", "Gucci", "Versace", "YSL", "Louis Vuitton",
//            "Burberry", "Prada", "Hermès", "Tom Ford", "Dolce & Gabbana", "Calvin Klein"
//        ));

        // Add blog posts
        model.addAttribute("blogPosts", List.of(
            Map.of(
                "id", 1,
                "title", "Cách chọn nước hoa phù hợp với từng mùa trong năm",
                "image", "/images/blog/blog1.jpg",
                "author", "Admin",
                "date", "15/04/2023",
                "excerpt", "Khám phá cách chọn nước hoa phù hợp với từng mùa để luôn tỏa hương thơm quyến rũ"
            ),
            Map.of(
                "id", 2,
                "title", "Top 5 nước hoa nam được yêu thích nhất 2023",
                "image", "/images/blog/blog2.jpg",
                "author", "Admin",
                "date", "10/04/2023",
                "excerpt", "Điểm qua những mùi hương nam tính đang được săn đón nhiều nhất trong năm nay"
            ),
            Map.of(
                "id", 3,
                "title", "Bí quyết giữ hương thơm nước hoa lâu hơn",
                "image", "/images/blog/blog3.jpg",
                "author", "Admin",
                "date", "05/04/2023",
                "excerpt", "Những mẹo đơn giản giúp nước hoa của bạn lưu hương suốt cả ngày dài"
            ),
            Map.of(
                "id", 4,
                "title", "Nước hoa - Món quà ý nghĩa cho người thân yêu",
                "image", "/images/blog/blog4.jpg",
                "author", "Admin",
                "date", "01/04/2023",
                "excerpt", "Gợi ý những chai nước hoa làm quà tặng cho những dịp đặc biệt"
            ),
            Map.of(
                "id", 5,
                "title", "Cách phân biệt nước hoa thật và giả",
                "image", "/images/blog/blog5.jpg",
                "author", "Admin",
                "date", "28/03/2023",
                "excerpt", "Những dấu hiệu giúp bạn nhận biết nước hoa chính hãng và tránh mua phải hàng giả"
            )
        ));

        return "home";
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

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return null;
    }
}
