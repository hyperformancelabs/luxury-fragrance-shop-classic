package com.hyperformancelabs.backend.controller;

import com.hyperformancelabs.backend.dto.*;
import com.hyperformancelabs.backend.model.Customer;
import com.hyperformancelabs.backend.model.Order;
import com.hyperformancelabs.backend.repository.CustomerRepository;
import com.hyperformancelabs.backend.repository.OrderRepository;
import com.hyperformancelabs.backend.service.CartService;
import com.hyperformancelabs.backend.service.OrderItemService;
import com.hyperformancelabs.backend.service.ProductService;
import com.hyperformancelabs.backend.service.ProductVariantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemService orderItemService;

    @GetMapping
    public String checkout(HttpServletRequest request, Model model) {
        String sessionId = getOrCreateSessionId(request);
        String username = getCurrentUsername();
        List<CartItemDTO> cartItems = cartService.getCartItems(username, sessionId);

        List<Map<String, Object>> enrichedItems = new ArrayList<>();
        for (CartItemDTO item : cartItems) {
            ProductVariantDTO variant = productVariantService.getProductVariantById(item.getProductVariantId());
            if (variant != null) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("cartItemId", item.getCartItemId());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("unitPrice", item.getUnitPrice());
                itemMap.put("total", item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                itemMap.put("variantId", variant.getProductVariantId());
                itemMap.put("volume", variant.getVolume());

                ProductDTO product = productService.getProductById(variant.getProductId());
                itemMap.put("productName", product.getProductName());
                itemMap.put("imageUrl", product.getImageUrl());

                enrichedItems.add(itemMap);
            }
        }

        int subtotal = enrichedItems.stream()
                .mapToInt(item -> ((BigDecimal) item.get("total")).intValue())
                .sum();
        int shipping = subtotal > 1000000 ? 0 : 30000;
        int total = subtotal + shipping;

        model.addAttribute("cartItems", enrichedItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shipping", shipping);
        model.addAttribute("total", total);
        model.addAttribute("itemCount", enrichedItems.size());

        System.out.println("JsessionId: " + sessionId);
        if (username != null) {
            List<Customer> customers = customerRepository.findByUsername(username);
            if (!customers.isEmpty()) {
                Customer customer = customers.get(0);
                String fullName = customer.getName();
                String firstName = "";
                String lastName = "";

                if (fullName != null && fullName.contains(" ")) {
                    int spaceIndex = fullName.indexOf(" ");
                    firstName = fullName.substring(0, spaceIndex).trim();
                    lastName = fullName.substring(spaceIndex + 1).trim();
                } else {
                    firstName = fullName; // nếu không có dấu cách thì xem toàn bộ là firstName
                    lastName = "";
                }

                model.addAttribute("firstName", firstName);
                model.addAttribute("lastName", lastName);
                model.addAttribute("email", customer.getEmail());
                model.addAttribute("phone", customer.getPhoneNumber());
                model.addAttribute("address", customer.getStreet());
                model.addAttribute("province", customer.getCity());
                model.addAttribute("district", customer.getDistrict());
                model.addAttribute("ward", customer.getWard());
            }
        }


        return "checkout/checkout";
    }

    @PostMapping
    @Transactional
    public String placeOrder(@ModelAttribute CheckoutFormDTO form,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        String sessionId = getOrCreateSessionId(request);
        String username = getCurrentUsername();
        Customer customer;

        if (username != null) {
            // Đã đăng nhập: lấy Customer entity từ username
            customer = customerRepository.findByUsername(username).get(0);
            if (customer == null) {
                throw new IllegalStateException("Không tìm thấy Customer với username: " + username);
            }

            customer.setName(form.getFirstName() + " " + form.getLastName());
            customer.setPhoneNumber(form.getPhone());
            customer.setEmail(form.getEmail());
            customer.setStreet(form.getAddress());
            customer.setWard(form.getWard());
            customer.setDistrict(form.getDistrict());
            customer.setCity(form.getProvince());
            customerRepository.save(customer);
        } else {
            // Chưa đăng nhập: tạo tài khoản guest
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            customer = new Customer();
            customer.setUsername("guest" + form.getPhone());
            customer.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            customer.setName(form.getFirstName() + " " + form.getLastName());
            customer.setPhoneNumber(form.getPhone());
            customer.setEmail(form.getEmail());
            customer.setStreet(form.getAddress());
            customer.setWard(form.getWard());
            customer.setDistrict(form.getDistrict());
            customer.setCity(form.getProvince());
            customer.setCreateAt(LocalDateTime.now());
            customer.setStatus("active");
            customer.setLoyaltyPoints(0);
            customer.setRating(0);
            customer.setNote("automatically created");
            customer = customerRepository.save(customer);
        }

        // Lấy giỏ hàng
        List<CartItemDTO> cartItems = cartService.getCartItems(username, sessionId);
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tạo đơn hàng
        Order order = new Order();
        order.setCustomer(customer);
        order.setShippingAddress(form.getAddress() + ", " + form.getWard() + ", " + form.getDistrict() + ", " + form.getProvince());
        order.setShippingOption(form.getPaymentMethod());
        order.setShippingFee(BigDecimal.ZERO);
        order.setTotalAmount(totalAmount);
        order.setOrderStatus("pending");
        order = orderRepository.save(order);

        // Lưu từng sản phẩm trong giỏ hàng
        for (CartItemDTO item : cartItems) {
            OrderItemDTO orderItem = new OrderItemDTO();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setProductVariantId(item.getProductVariantId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(item.getUnitPrice());
            orderItemService.save(orderItem);
        }

        // Xoá giỏ hàng sau khi đặt hàng
        for (CartItemDTO item : cartItems) {
            cartService.removeCartItem(item.getCartItemId(), username, sessionId);
        }

        if (order.getOrderId() == null) return "redirect:/checkout";

        System.out.println("Redirecting to order-success with orderId = " + order.getOrderId());
        System.out.println("username: " + username);
        return "redirect:/order-success?orderId=" + order.getOrderId();
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
}
