package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.CartItemDTO;
import com.hyperformancelabs.backend.model.Cart;
import com.hyperformancelabs.backend.model.CartItem;
import com.hyperformancelabs.backend.model.ProductVariant;
import com.hyperformancelabs.backend.repository.CartItemRepository;
import com.hyperformancelabs.backend.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartItemServiceImpl implements CartItemService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public CartItemDTO findByCartAndProductVariant(Cart cart, ProductVariant productVariant) {
        return cartItemRepository.findByCartAndProductVariant(cart, productVariant)
                .map(this::convertToCartItemDTO)
                .orElse(null);
    }

    @Override
    public List<CartItemDTO> findByCart(Cart cart) {
        return cartItemRepository.findByCart(cart)
                .stream()
                .map(this::convertToCartItemDTO)
                .collect(Collectors.toList());
    }

    private CartItemDTO convertToCartItemDTO(CartItem cartItem) {
        return new CartItemDTO(
                cartItem.getCartItemId(),
                cartItem.getCart().getCartId(),
                cartItem.getProductVariant().getProductVariantId(),
                cartItem.getQuantity(),
                cartItem.getUnitPrice(),
                cartItem.getNote(),
                cartItem.getIsSelected()
        );
    }
}
