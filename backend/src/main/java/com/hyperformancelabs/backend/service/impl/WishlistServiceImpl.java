package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.WishlistDTO;
import com.hyperformancelabs.backend.exception.ResourceNotFoundException;
import com.hyperformancelabs.backend.model.Customer;
import com.hyperformancelabs.backend.model.Wishlist;
import com.hyperformancelabs.backend.repository.CustomerRepository;
import com.hyperformancelabs.backend.repository.ProductVariantRepository;
import com.hyperformancelabs.backend.repository.WishlistRepository;
import com.hyperformancelabs.backend.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Override
    public void addToWishlist(String username, Integer productVariantId) {

        Integer customerId = customerRepository.findTopByUsernameOrderByCustomerIdDesc(username)
                .map(Customer::getCustomerId)
                .orElse(null);

        WishlistDTO wishlistDTO = new WishlistDTO();
        wishlistDTO.setCustomerId(customerId);
        wishlistDTO.setProductVariantId(productVariantId);
        wishlistRepository.save(convertToWishlist(wishlistDTO));
    }

    @Override
    public void removeWishlistItem(String username, Integer productVariantId){
        Integer customerId = customerRepository.findTopByUsernameOrderByCustomerIdDesc(username)
                .map(Customer::getCustomerId)
                .orElse(null);
        wishlistRepository.deleteByCustomer_CustomerIdAndProductVariant_ProductVariantId(customerId, productVariantId);
    }

    @Override
    public List<WishlistDTO> getWishlistItems(String username) {
        Integer customerId = customerRepository.findTopByUsernameOrderByCustomerIdDesc(username)
                .map(Customer::getCustomerId)
                .orElse(null);
        return wishlistRepository.findByCustomer_CustomerId(customerId)
                .stream()
                .map(this::convertToWishlistDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void clearWishlist(Integer customerId) {
        wishlistRepository.deleteByCustomer_CustomerId(customerId);
    }

    private Wishlist convertToWishlist(WishlistDTO wishlistDTO) {
        return new Wishlist(
                wishlistDTO.getWishlistId(),
                customerRepository.findByCustomerId(wishlistDTO.getCustomerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + wishlistDTO.getCustomerId())),
                productVariantRepository.findByProductVariantId(wishlistDTO.getProductVariantId()),
                wishlistDTO.getAddedDate()
        );
    }

    private WishlistDTO convertToWishlistDTO(Wishlist wishlist) {
        return new WishlistDTO(
                wishlist.getWishlistId(),
                wishlist.getCustomer().getCustomerId(),
                wishlist.getProductVariant().getProductVariantId(),
                wishlist.getAddedDate()
        );
    }
}
