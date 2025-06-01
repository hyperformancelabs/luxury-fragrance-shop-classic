package com.hyperformancelabs.backend.controller.user;

import com.hyperformancelabs.backend.dto.common.response.CustomerDTO;
import com.hyperformancelabs.backend.dto.common.response.OrderDTO;
import com.hyperformancelabs.backend.dto.common.response.ProductDTO;
import com.hyperformancelabs.backend.dto.common.response.ProductVariantDTO;
import com.hyperformancelabs.backend.dto.user.response.WishlistDTO;
import com.hyperformancelabs.backend.dto.user.response.WishlistItemDisplayDTO;
import com.hyperformancelabs.backend.exception.ResourceNotFoundException;
import com.hyperformancelabs.backend.model.Customer;
import com.hyperformancelabs.backend.model.Order;
import com.hyperformancelabs.backend.service.CustomerService;
import com.hyperformancelabs.backend.service.OrderService;
import com.hyperformancelabs.backend.service.ProductService;
import com.hyperformancelabs.backend.service.ProductVariantService;
import com.hyperformancelabs.backend.service.WishlistService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private WishlistService wishlistService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductVariantService productVariantService;

    @GetMapping
    public String viewProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        CustomerDTO customer = customerService.getCustomerByUsername(username);
        model.addAttribute("user", customer);
        return "user/profile/profile";
    }

    @GetMapping("/orders")
    public String viewOrders(Model model, Authentication authentication) {
        String username = authentication.getName();
        CustomerDTO customer = customerService.getCustomerByUsername(username);
        List<OrderDTO> orders = orderService.findOrdersByCustomerId(customer.getCustomerId());
        model.addAttribute("orders", orders);
        model.addAttribute("user", customer);
        return "user/profile/orders";
    }
    
    @GetMapping("/wishlist")
    public String viewWishlist(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            CustomerDTO customer = customerService.getCustomerByUsername(username);
            
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

                    WishlistItemDisplayDTO displayDTO = new WishlistItemDisplayDTO();
                    displayDTO.setWishlistId(wishlistItem.getWishlistId());
                    displayDTO.setProductId(product.getProductId());
                    displayDTO.setProductVariantId(productVariant.getProductVariantId());
                    displayDTO.setProductName(product.getProductName());
                    displayDTO.setImageUrl(product.getImageUrl());
                    displayDTO.setVolume(productVariant.getVolume());
                    displayDTO.setUnitPrice(productVariant.getPrice());
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
            model.addAttribute("user", customer);
            
            return "user/profile/wishlist";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi tải danh sách yêu thích: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/profile";
        }
    }

    @GetMapping("/edit")
    public String editProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        CustomerDTO customer = customerService.getCustomerByUsername(username);
        model.addAttribute("user", customer);
        return "user/profile/edit-profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam String username,
                                @RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String phoneNumber,
                                @RequestParam String street,
                                @RequestParam String ward,
                                @RequestParam String district,
                                @RequestParam String city,
                                RedirectAttributes redirectAttributes) {

        CustomerDTO customer = customerService.getCustomerByUsername(username);
        if (customer == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng.");
            return "redirect:/profile/edit";
        }

        // Cập nhật thông tin cơ bản
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhoneNumber(phoneNumber);
        customer.setStreet(street);
        customer.setWard(ward);
        customer.setDistrict(district);
        customer.setCity(city);

        customerService.updateCustomer(customer);
        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công.");
        return "redirect:/profile";
    }
} 