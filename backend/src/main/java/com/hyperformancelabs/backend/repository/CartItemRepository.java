package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.model.Cart;
import com.hyperformancelabs.backend.model.CartItem;
import com.hyperformancelabs.backend.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCartAndProductVariant(Cart cart, ProductVariant productVariant);
    List<CartItem> findByCart(Cart cart);
}
