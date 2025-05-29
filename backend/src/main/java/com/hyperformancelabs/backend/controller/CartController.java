package com.hyperformancelabs.backend.controller;

import com.hyperformancelabs.backend.dto.*;
import com.hyperformancelabs.backend.dto.request.AddToCartRequest;
import com.hyperformancelabs.backend.exception.ResourceNotFoundException;
import com.hyperformancelabs.backend.service.BrandService;
import com.hyperformancelabs.backend.service.CartService;
import com.hyperformancelabs.backend.service.ProductService;
import com.hyperformancelabs.backend.service.ProductVariantService;

import java.util.ArrayList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @GetMapping
    public String viewCart(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin người dùng và session
            String username = getCurrentUsername();
            String sessionId = getOrCreateSessionId(request);

            // Lấy danh sách sản phẩm trong giỏ hàng
            List<CartItemDTO> cartItems = cartService.getCartItems(username, sessionId);

            // Chuyển đổi CartItemDTO sang CartItemDisplayDTO
            List<CartItemDisplayDTO> cartItemsDisplay = new ArrayList<>();

            for (CartItemDTO cartItem : cartItems) {
                try {
                    // Truy vấn thông tin ProductVariant
                    ProductVariantDTO productVariant = productVariantService.getProductVariantById(cartItem.getProductVariantId());

                    // Truy vấn thông tin Product
                    ProductDTO product = productService.getProductById(productVariant.getProductId());

                    // Truy vấn thông tin Brand
                    BrandDTO brand = brandService.getBrandByBrandName(product.getBrandName());

                    // Tạo CartItemDisplayDTO từ CartItemDTO, ProductVariantDTO và ProductDTO
                    CartItemDisplayDTO displayDTO = new CartItemDisplayDTO();
                    displayDTO.setCartItemId(cartItem.getCartItemId());
                    displayDTO.setProductId(product.getProductId());
                    displayDTO.setProductVariantId(productVariant.getProductVariantId());
                    displayDTO.setProductName(product.getProductName());
                    displayDTO.setBrandName(brand.getBrandName());
                    displayDTO.setImageUrl(product.getImageUrl());
                    displayDTO.setVolume(productVariant.getVolume());
                    displayDTO.setQuantity(cartItem.getQuantity());
                    displayDTO.setUnitPrice(productVariant.getPrice());
                    displayDTO.setTotalPrice(productVariant.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));
                    displayDTO.setIsSelected(cartItem.getIsSelected());
                    displayDTO.setNote(cartItem.getNote());

                    cartItemsDisplay.add(displayDTO);
                } catch (ResourceNotFoundException e) {
                    // Xử lý khi không tìm thấy sản phẩm hoặc biến thể
                    e.printStackTrace();
                }
            }

            // Lấy thông tin giỏ hàng
            CartDTO cart = cartService.getCart(username, sessionId);

            // Tính tổng tiền
            BigDecimal subtotal = cartItemsDisplay.stream()
                    .filter(CartItemDisplayDTO::getIsSelected)
                    .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Tính phí vận chuyển (giả sử miễn phí vận chuyển cho đơn hàng trên 1.000.000đ)
            BigDecimal shipping = subtotal.compareTo(new BigDecimal(1000000)) > 0 ?
                    BigDecimal.ZERO : new BigDecimal(30000);

            // Tổng cộng
            BigDecimal total = subtotal.add(shipping);

            model.addAttribute("cartItems", cartItemsDisplay);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shipping", shipping);
            model.addAttribute("total", total);
            model.addAttribute("itemCount", cartItemsDisplay.size());

            return "cart/cart";
        } catch (Exception e) {
            // Xử lý lỗi
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi tải giỏ hàng: " + e.getMessage());
            e.printStackTrace();

            // Trả về trang giỏ hàng trống
            model.addAttribute("cartItems", List.of());
            model.addAttribute("subtotal", BigDecimal.ZERO);
            model.addAttribute("shipping", BigDecimal.ZERO);
            model.addAttribute("total", BigDecimal.ZERO);
            model.addAttribute("itemCount", 0);

            return "cart/cart";
        }
    }

    @PostMapping("/add")
    public String addToCart(
            @RequestParam Integer productVariantId,
            @RequestParam Integer quantity,
            HttpServletRequest request) {

        String username = getCurrentUsername();
        String sessionId = getOrCreateSessionId(request);

        System.out.println("Adding to cart: " + productVariantId + " - " + quantity);
        System.out.println("Username: " + username + " - SessionId: " + sessionId);

        AddToCartRequest addToCartRequest = new AddToCartRequest();
        addToCartRequest.setProductVariantId(productVariantId);
        addToCartRequest.setQuantity(quantity);

        try {
            cartService.addToCart(username, sessionId, addToCartRequest);
        } catch (Exception e) {
            // Nếu có lỗi, vẫn quay lại trang trước, nhưng kèm thông báo lỗi
            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null ? referer.split("\\?")[0] + "?error=true" : "/?error=true");
        }

        // ✅ Thêm tham số ?addedToCart=success để hiển thị thông báo SweetAlert
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer.split("\\?")[0] + "?addedToCart=success" : "/?addedToCart=success");
    }



    @PostMapping("/update")
    public String updateCartItemQuantity(
            @RequestParam Integer cartItemId,
            @RequestParam Integer quantity,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (quantity <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số lượng phải lớn hơn 0");
            return "redirect:/cart";
        }

        String username = getCurrentUsername();
        String sessionId = getOrCreateSessionId(request);

        try {
            cartService.updateCartItemQuantity(cartItemId, quantity, username, sessionId);
            redirectAttributes.addFlashAttribute("successMessage", "Số lượng đã được cập nhật");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật số lượng: " + e.getMessage());
        }

        return "redirect:/cart";
    }


    @PostMapping("/remove")
    public String removeCartItem(
            @RequestParam Integer cartItemId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String username = getCurrentUsername();
        String sessionId = getOrCreateSessionId(request);

        try {
            cartService.removeCartItem(cartItemId, username, sessionId);
            redirectAttributes.addFlashAttribute("successMessage", "Sản phẩm đã được xóa khỏi giỏ hàng");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm: " + e.getMessage());
        }

        return "redirect:/cart";
    }


    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            return authentication.getName();
        }
        return null;
    }

    private String getOrCreateSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String sessionId = (String) session.getAttribute("CART_SESSION_ID");

        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            session.setAttribute("CART_SESSION_ID", sessionId);
        }

        return sessionId;
    }

    @PostMapping("/add-by-product")
    public String addToCartByProduct(@RequestParam(value = "productId", required = true) Integer productId,
                                    @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        try {
            if (productId == null) {
                throw new IllegalArgumentException("ID sản phẩm không hợp lệ");
            }

            // Lấy thông tin người dùng và session
            String username = getCurrentUsername();
            String sessionId = getOrCreateSessionId(request);

            // Lấy variant đầu tiên của sản phẩm
            ProductVariantDTO variant = productVariantService.findFirstByProduct_ProductId(productId);
            if (variant == null) {
                throw new IllegalArgumentException("Không tìm thấy biến thể sản phẩm");
            }

            // Tạo request để thêm vào giỏ hàng
            AddToCartRequest addToCartRequest = new AddToCartRequest();
            addToCartRequest.setProductVariantId(variant.getProductVariantId());
            addToCartRequest.setQuantity(quantity);

            // Thêm vào giỏ hàng
            cartService.addToCart(username, sessionId, addToCartRequest);

            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng");
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thêm sản phẩm vào giỏ hàng: " + e.getMessage());

            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null ? referer : "/shop");
        }
    }
}
