package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.CartDTO;
import com.hyperformancelabs.backend.dto.CartItemDTO;
import com.hyperformancelabs.backend.dto.request.AddToCartRequest;

import java.util.List;

public interface CartService {

    // Thêm sản phẩm vào giỏ hàng
    void addToCart(String username, String sessionId, AddToCartRequest request);

    // Lấy danh sách sản phẩm trong giỏ hàng
    List<CartItemDTO> getCartItems(String username, String sessionId);

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    void updateCartItemQuantity(Integer cartItemId, Integer quantity, String username, String sessionId);

    // Xóa sản phẩm khỏi giỏ hàng
    void removeCartItem(Integer cartItemId, String username, String sessionId);

    // Lấy thông tin giỏ hàng
    CartDTO getCart(String username, String sessionId);
}