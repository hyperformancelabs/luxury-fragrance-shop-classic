package com.hyperformancelabs.backend.controller.user;

import com.hyperformancelabs.backend.dto.user.response.WishlistDTO;
import com.hyperformancelabs.backend.dto.common.response.ProductVariantDTO;
import com.hyperformancelabs.backend.dto.common.response.ProductDTO;
import com.hyperformancelabs.backend.dto.common.response.FlashSaleProductDTO;
import com.hyperformancelabs.backend.dto.admin.common.InventoryTransactionDTO;
import com.hyperformancelabs.backend.dto.common.response.BrandDTO;

import com.hyperformancelabs.backend.model.ProductVariant;
import com.hyperformancelabs.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
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

        // ------------------- Get flash deal products - fix duplicate products ----------------------------------------
        List<FlashSaleProductDTO> flashDealProducts = productService.getFlashSaleProducts();
        
        // Debug: Log flash sale data
        System.out.println("=== FLASH SALE DEBUG ===");
        System.out.println("Total flash deal products from service: " + flashDealProducts.size());
        for (FlashSaleProductDTO dto : flashDealProducts) {
            System.out.println("Flash sale product: ID=" + dto.getProductId() + ", Name=" + dto.getProductName());
        }
        
        // Get unique product IDs from flash sale products to avoid duplicates
        Set<Integer> uniqueFlashSaleProductIds = flashDealProducts.stream()
                .map(FlashSaleProductDTO::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        System.out.println("Unique flash sale product IDs: " + uniqueFlashSaleProductIds);
        
        // Get unique flash sale products
        List<ProductDTO> flashSaleProducts = uniqueFlashSaleProductIds.stream()
                .map(productService::getProductById)
                .filter(Objects::nonNull)
                .limit(6) // Limit to 6 products to avoid too many
                .collect(Collectors.toList());

        System.out.println("Final flash sale products count: " + flashSaleProducts.size());
        System.out.println("=== END FLASH SALE DEBUG ===");

        // Create a proper map for flash sale product variants - fix duplication issue
        Map<Integer, List<ProductVariantDTO>> flashSaleProductVariantMap = new HashMap<>();
        for (ProductDTO product : flashSaleProducts) {
            if (product != null) {
                List<ProductVariantDTO> variants = productVariantService.getProductVariantsByProductId(product.getProductId());
                if (variants != null && !variants.isEmpty()) {
                    flashSaleProductVariantMap.put(product.getProductId(), variants);
                }
            }
        }

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


        // ---------------------------------- Get new products - fix duplicate products -------------------------------------------------------------------
        List<InventoryTransactionDTO> inventoryTransactions = inventoryTransactionService.findTop6ImportTransactionsNative();

        // Get unique product IDs from inventory transactions - ensure no duplicates
        Set<Integer> uniqueNewProductIds = new LinkedHashSet<>(); // Use LinkedHashSet to maintain order
        for (InventoryTransactionDTO transaction : inventoryTransactions) {
            if (transaction != null && transaction.getProductVariantId() != null) {
                ProductVariantDTO variant = productVariantService.getProductVariantById(transaction.getProductVariantId());
                if (variant != null && variant.getProductId() != null) {
                    uniqueNewProductIds.add(variant.getProductId());
                }
            }
        }

        // Get unique new products in the order they were added
        List<ProductDTO> newProducts = uniqueNewProductIds.stream()
                .map(productService::getProductById)
                .filter(Objects::nonNull)
                .limit(6) // Limit to 6 products to avoid too many
                .collect(Collectors.toList());

        Map<Integer, List<ProductVariantDTO>> newProductVariantMap = new HashMap<>();
        for (ProductDTO product : newProducts) {
            if (product != null) {
                List<ProductVariantDTO> variants = productVariantService.getProductVariantsByProductId(product.getProductId());
                if (variants != null && !variants.isEmpty()) {
                    newProductVariantMap.put(product.getProductId(), variants);
                }
            }
        }

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


        // --------------------------------------------- Season logic ------------------------------------
        Map<String, Object> seasonData = getCurrentSeasonData();
        model.addAttribute("currentSeason", seasonData.get("currentSeason"));
        model.addAttribute("nextSeason", seasonData.get("nextSeason"));
        model.addAttribute("currentSeasonVietnamese", seasonData.get("currentSeasonVietnamese"));
        model.addAttribute("nextSeasonVietnamese", seasonData.get("nextSeasonVietnamese"));
        
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

        return "user/home/home";
    }

    @GetMapping("/debug/flash-sale")
    @ResponseBody
    public Map<String, Object> debugFlashSale() {
        List<FlashSaleProductDTO> flashDealProducts = productService.getFlashSaleProducts();
        
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("totalFlashSaleProducts", flashDealProducts.size());
        debugInfo.put("flashSaleProducts", flashDealProducts);
        
        Set<Integer> uniqueProductIds = flashDealProducts.stream()
                .map(FlashSaleProductDTO::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        debugInfo.put("uniqueProductIds", uniqueProductIds);
        debugInfo.put("uniqueProductCount", uniqueProductIds.size());
        
        return debugInfo;
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

    /**
     * Tính toán mùa hiện tại và mùa tiếp theo
     * Xuân: tháng 1-3, Hạ: tháng 4-6, Thu: tháng 7-9, Đông: tháng 10-12
     */
    private Map<String, Object> getCurrentSeasonData() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        
        String currentSeason;
        String nextSeason;
        String currentSeasonVietnamese;
        String nextSeasonVietnamese;
        
        if (month >= 1 && month <= 3) {
            // Xuân
            currentSeason = "spring";
            nextSeason = "summer";
            currentSeasonVietnamese = "Xuân";
            nextSeasonVietnamese = "Hạ";
        } else if (month >= 4 && month <= 6) {
            // Hạ
            currentSeason = "summer";
            nextSeason = "autumn";
            currentSeasonVietnamese = "Hạ";
            nextSeasonVietnamese = "Thu";
        } else if (month >= 7 && month <= 9) {
            // Thu
            currentSeason = "autumn";
            nextSeason = "winter";
            currentSeasonVietnamese = "Thu";
            nextSeasonVietnamese = "Đông";
        } else {
            // Đông (tháng 10-12)
            currentSeason = "winter";
            nextSeason = "spring";
            currentSeasonVietnamese = "Đông";
            nextSeasonVietnamese = "Xuân";
        }
        
        Map<String, Object> seasonData = new HashMap<>();
        seasonData.put("currentSeason", currentSeason);
        seasonData.put("nextSeason", nextSeason);
        seasonData.put("currentSeasonVietnamese", currentSeasonVietnamese);
        seasonData.put("nextSeasonVietnamese", nextSeasonVietnamese);
        
        return seasonData;
    }
} 