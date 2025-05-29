package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.CartItemDTO;
import com.hyperformancelabs.backend.model.Cart;
import com.hyperformancelabs.backend.model.CartItem;
import com.hyperformancelabs.backend.model.ProductVariant;

import java.util.List;
import java.util.Optional;

public interface CartItemService {
    // Lấy sản phẩm trong giỏ hàng
    CartItemDTO findByCartAndProductVariant(Cart cart, ProductVariant productVariant);

    // Lấy danh sách sản phẩm trong giỏ hàng
    List<CartItemDTO> findByCart(Cart cart);
}
