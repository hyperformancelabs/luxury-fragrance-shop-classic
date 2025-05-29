package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.WishlistDTO;

import java.util.List;

public interface WishlistService {
    // Lấy danh sách sản phẩm trong wishlist
    List<WishlistDTO> getWishlistItems(String username);

    // Thêm sản phẩm vào wishlist
    void addToWishlist(String username, Integer productVariantId);

    // Xóa sản phẩm khỏi wishlist
    void removeWishlistItem(String username, Integer productVariantId);

    // Xóa toàn bộ wishlist
    void clearWishlist(Integer customerId);
}
