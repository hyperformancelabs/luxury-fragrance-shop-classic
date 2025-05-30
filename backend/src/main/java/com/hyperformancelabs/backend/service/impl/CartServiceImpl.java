package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.user.response.CartDTO;
import com.hyperformancelabs.backend.dto.user.response.CartItemDTO;
import com.hyperformancelabs.backend.dto.user.request.AddToCartRequest;
import com.hyperformancelabs.backend.model.Cart;
import com.hyperformancelabs.backend.model.CartItem;
import com.hyperformancelabs.backend.model.Customer;
import com.hyperformancelabs.backend.model.ProductVariant;
import com.hyperformancelabs.backend.repository.CartItemRepository;
import com.hyperformancelabs.backend.repository.CartRepository;
import com.hyperformancelabs.backend.repository.CustomerRepository;
import com.hyperformancelabs.backend.repository.ProductVariantRepository;
import com.hyperformancelabs.backend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public void addToCart(String username, String sessionId, AddToCartRequest request) {
        // Lấy hoặc tạo giỏ hàng
        Cart cart = getOrCreateCart(username, sessionId);

        // Lấy sản phẩm variant
        ProductVariant productVariant = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

        // Kiểm tra tồn kho
        if (productVariant.getQuantityInStock() < request.getQuantity()) {
            throw new RuntimeException("Số lượng vượt quá tồn kho. Hiện chỉ còn " + productVariant.getQuantityInStock() + " sản phẩm.");
        }

        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        CartItem existingCartItem = cartItemRepository.findByCartAndProductVariant(cart, productVariant).orElse(null);

        if (existingCartItem != null) {
            // Nếu đã có, cập nhật số lượng
            int newQuantity = existingCartItem.getQuantity() + request.getQuantity();
            
            // Kiểm tra lại số lượng mới có vượt quá tồn kho không
            if (newQuantity > productVariant.getQuantityInStock()) {
                throw new RuntimeException("Tổng số lượng vượt quá tồn kho. Hiện chỉ còn " + productVariant.getQuantityInStock() + " sản phẩm.");
            }
            
            existingCartItem.setQuantity(newQuantity);
            cartItemRepository.save(existingCartItem);
        } else {
            // Nếu chưa có, tạo mới cart item
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProductVariant(productVariant);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setUnitPrice(productVariant.getPrice());
            cartItem.setIsSelected(true);
            cartItemRepository.save(cartItem);
        }

        // Cập nhật tổng tiền của giỏ hàng
        updateCartTotalAmount(cart);
    }

    @Override
    public List<CartItemDTO> getCartItems(String username, String sessionId) {
        Cart cart = getOrCreateCart(username, sessionId);
        return cartItemRepository.findByCart(cart)
                .stream()
                .map(this::convertToCartItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateCartItemQuantity(Integer cartItemId, Integer quantity, String username, String sessionId) {
        Cart cart = getOrCreateCart(username, sessionId);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Kiểm tra số lượng tồn kho
        ProductVariant productVariant = cartItem.getProductVariant();
        if (productVariant.getQuantityInStock() < quantity) {
            throw new RuntimeException("Số lượng vượt quá tồn kho. Hiện chỉ còn " + productVariant.getQuantityInStock() + " sản phẩm.");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        // Cập nhật tổng tiền của giỏ hàng
        updateCartTotalAmount(cart);

        convertToCartItemDTO(cartItem);
    }

    @Override
    public void removeCartItem(Integer cartItemId, String username, String sessionId) {
        Cart cart = getOrCreateCart(username, sessionId);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cartItemRepository.delete(cartItem);

        // Cập nhật tổng tiền của giỏ hàng
        updateCartTotalAmount(cart);
    }

    @Override
    public CartDTO getCart(String username, String sessionId) {
        Cart cart = getOrCreateCart(username, sessionId);
        return convertToDTO(cart);
    }

    public Cart getOrCreateCart(String username, String sessionId) {
        Cart cart = null;
        
        // Nếu có username (đã đăng nhập), tìm giỏ hàng theo customer
        if (username != null) {
            try {
                List<Customer> customers = customerRepository.findByUsername(username);
                if (!customers.isEmpty()) {
                    Customer customer = customers.get(0);
                    cart = cartRepository.findTopByCustomerAndStatusOrderByCartIdDesc(customer, "active")
                            .orElse(null);
                    
                    if (cart == null) {
                        cart = new Cart();
                        cart.setCustomer(customer);
                        cart.setStatus("active");
                        cart.setSessionId(sessionId);
                        cartRepository.save(cart);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Nếu không có username hoặc không tìm thấy customer, tìm giỏ hàng theo sessionId
        if (cart == null) {
            try {
                cart = cartRepository.findTopBySessionIdOrderByCartIdDesc(sessionId)
                        .orElse(null);
                System.out.println("Cart found: " + sessionId);
                if (cart == null) {
                    cart = new Cart();
                    cart.setStatus("active");
                    cart.setSessionId(sessionId);
                    cart.setTotalAmount(BigDecimal.ZERO);
                    cartRepository.save(cart);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return cart;
    }

    private void updateCartTotalAmount(Cart cart) {
        BigDecimal totalAmount = cartItemRepository.findByCart(cart)
                .stream()
                .map(cartItem -> cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(totalAmount);
        cartRepository.save(cart);
    }

    private CartDTO convertToDTO(Cart cart) {
        Integer customerId = null;
        if (cart.getCustomer() != null) {
            customerId = cart.getCustomer().getCustomerId();
        }
        
        return new CartDTO(
                cart.getCartId(),
                customerId,
                cart.getStatus(),
                cart.getTotalAmount(),
                cart.getSessionId()
        );
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
