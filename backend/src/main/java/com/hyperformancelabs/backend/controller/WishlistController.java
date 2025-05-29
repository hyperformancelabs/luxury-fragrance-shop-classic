package com.hyperformancelabs.backend.controller;

import com.hyperformancelabs.backend.dto.*;
import com.hyperformancelabs.backend.exception.ResourceNotFoundException;
import com.hyperformancelabs.backend.service.CustomerService;
import com.hyperformancelabs.backend.service.ProductService;
import com.hyperformancelabs.backend.service.ProductVariantService;
import com.hyperformancelabs.backend.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public String viewWishlist(Model model, RedirectAttributes redirectAttributes) {
        try {
            String username = getCurrentUsername();

            List<WishlistDTO> wishlistItems = wishlistService.getWishlistItems(username);
            List<WishlistItemDisplayDTO> wishlistItemDisplays = new ArrayList<>();

            boolean hasInStock = false;

            for (WishlistDTO wishlistItem : wishlistItems) {
                try {
                    ProductVariantDTO productVariant = productVariantService.getProductVariantById(wishlistItem.getProductVariantId());
                    ProductDTO product = productService.getProductById(productVariant.getProductId());

                    List<ProductVariantDTO> variants = productVariantService.getProductVariantsByProductId(product.getProductId());

                    List<Integer> volumes = variants.stream()
                            .map(ProductVariantDTO::getVolume)
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList());

                    BigDecimal minPrice = variants.stream()
                            .map(ProductVariantDTO::getPrice)
                            .min(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO);

                    BigDecimal maxPrice = variants.stream()
                            .map(ProductVariantDTO::getPrice)
                            .max(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO);

                    boolean anyVariantInStock = variants.stream()
                            .anyMatch(v -> v.getQuantityInStock() > 0);

                    Map<String, Integer> volumeVariantMap = variants.stream()
                            .collect(Collectors.toMap(
                                    v -> String.valueOf(v.getVolume()),
                                    ProductVariantDTO::getProductVariantId,
                                    (a, b) -> a
                            ));

                    System.out.println("Volume Variant Map: " + volumeVariantMap.keySet());

                    WishlistItemDisplayDTO displayDTO = getWishlistItemDisplayDTO(wishlistItem, product, productVariant);
                    displayDTO.setVariantVolumes(volumes);
                    displayDTO.setVolumeVariantIdMap(volumeVariantMap);
                    displayDTO.setMinPrice(minPrice);
                    displayDTO.setMaxPrice(maxPrice);
                    displayDTO.setInStock(anyVariantInStock);

                    if (anyVariantInStock) {
                        hasInStock = true;
                    }

                    wishlistItemDisplays.add(displayDTO);

                } catch (ResourceNotFoundException e) {
                    e.printStackTrace();
                }
            }

            model.addAttribute("hasInStock", hasInStock);
            model.addAttribute("wishlistItems", wishlistItemDisplays);

            System.out.println("hasInStock" + hasInStock);
            return "wishlist/wishlist";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi tải giỏ hàng: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("wishlistItems", List.of());
            return "wishlist/wishlist";
        }
    }

    @PostMapping("/add")
    public String addToWishlist(
            @RequestParam Integer productId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            String username = getCurrentUsername();
            Integer productVariantId = productVariantService.findFirstByProduct_ProductId(productId).getProductVariantId();
            wishlistService.addToWishlist(username, productVariantId);
            redirectAttributes.addFlashAttribute("wishlistAdded", "success");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm sản phẩm vào danh sách yêu thích: " + e.getMessage());
        }


        String referer = request.getHeader("Referer");
        if (referer != null) {
            if (referer.contains("?")) {
                return "redirect:" + referer + "&wishlistAdded=success";
            } else {
                return "redirect:" + referer + "?wishlistAdded=success";
            }
        }
        return "redirect:/?wishlistAdded=success";
    }

    @PostMapping("/remove")
    @Transactional
    public String removeWishlistItem(
            @RequestParam Integer productVariantId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            String username = getCurrentUsername();
            wishlistService.removeWishlistItem(username, productVariantId);
            redirectAttributes.addFlashAttribute("wishlistRemoved", "success");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm khỏi danh sách yêu thích: " + e.getMessage());
        }

        String referer = request.getHeader("Referer");
        if (referer != null) {
            if (referer.contains("?")) {
                return "redirect:" + referer + "&wishlistRemoved=success";
            } else {
                return "redirect:" + referer + "?wishlistRemoved=success";
            }
        }
        return "redirect:/?wishlistRemoved=success";
    }

    @PostMapping("/clear")
    @Transactional
    public String clearWishlist(RedirectAttributes redirectAttributes) {
        try {
            String username = getCurrentUsername();
            CustomerDTO customer = customerService.getCustomerByUsername(username);
            wishlistService.clearWishlist(customer.getCustomerId());
            System.out.println("Cleared wishlist for customer: " + customer.getCustomerId());
            redirectAttributes.addFlashAttribute("wishlistCleared", "success");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa danh sách yêu thích: " + e.getMessage());
        }

        return "redirect:/wishlist?wishlistCleared=success";
    }

    private static WishlistItemDisplayDTO getWishlistItemDisplayDTO(WishlistDTO wishlistItem, ProductDTO product, ProductVariantDTO productVariant) {
        WishlistItemDisplayDTO displayDTO = new WishlistItemDisplayDTO();
        displayDTO.setWishlistId(wishlistItem.getWishlistId());
        displayDTO.setProductId(product.getProductId());
        displayDTO.setProductVariantId(productVariant.getProductVariantId());
        displayDTO.setProductName(product.getProductName());
        displayDTO.setImageUrl(product.getImageUrl());
        displayDTO.setVolume(productVariant.getVolume());
        displayDTO.setUnitPrice(productVariant.getPrice());
        return displayDTO;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return null;
    }
}
