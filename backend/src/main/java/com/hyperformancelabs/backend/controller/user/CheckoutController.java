package com.hyperformancelabs.backend.controller.user;

import com.hyperformancelabs.backend.dto.user.response.CartItemDTO;
import com.hyperformancelabs.backend.dto.common.response.ProductVariantDTO;
import com.hyperformancelabs.backend.dto.common.response.ProductDTO;
import com.hyperformancelabs.backend.dto.user.request.CheckoutFormDTO;
import com.hyperformancelabs.backend.dto.common.response.OrderItemDTO;
import com.hyperformancelabs.backend.dto.common.response.BreadcrumbItemDTO;

import com.hyperformancelabs.backend.model.Customer;
import com.hyperformancelabs.backend.model.Employee;
import com.hyperformancelabs.backend.model.Order;
import com.hyperformancelabs.backend.repository.CustomerRepository;
import com.hyperformancelabs.backend.repository.EmployeeRepository;
import com.hyperformancelabs.backend.repository.OrderRepository;
import com.hyperformancelabs.backend.service.CartService;
import com.hyperformancelabs.backend.service.EmailService;
import com.hyperformancelabs.backend.service.OrderItemService;
import com.hyperformancelabs.backend.service.ProductService;
import com.hyperformancelabs.backend.service.ProductVariantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.regex.Pattern;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemService orderItemService;
    
    @Autowired
    private EmailService emailService;

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
        
        // Add breadcrumb paths
        List<BreadcrumbItemDTO> breadcrumbPaths = List.of(
            new BreadcrumbItemDTO("GIỎ HÀNG", "/cart"),
            BreadcrumbItemDTO.createCurrentPage("THANH TOÁN")
        );
        model.addAttribute("breadcrumbPaths", breadcrumbPaths);

        if (username != null) {
            List<Customer> customers = customerRepository.findByUsername(username);
            if (!customers.isEmpty()) {
                Customer customer = customers.get(0);
                model.addAttribute("fullName", customer.getName());
                model.addAttribute("email", customer.getEmail());
                model.addAttribute("phone", customer.getPhoneNumber());
                model.addAttribute("address", customer.getStreet());
                model.addAttribute("province", customer.getCity());
                model.addAttribute("district", customer.getDistrict());
                model.addAttribute("ward", customer.getWard());
                model.addAttribute("isLoggedIn", true);
            }
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        return "user/checkout/checkout";
    }

    @PostMapping
    @Transactional
    public String placeOrder(@ModelAttribute CheckoutFormDTO form,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        String sessionId = getOrCreateSessionId(request);
        String username = getCurrentUsername();
        Customer customer;
        
        // Validate email format if provided
        if (form.getEmail() != null && !form.getEmail().isEmpty() && !EMAIL_PATTERN.matcher(form.getEmail()).matches()) {
            redirectAttributes.addFlashAttribute("error", "Định dạng email không hợp lệ");
            return "redirect:/checkout";
        }

        try {
            if (username != null) {
                // Người dùng đã đăng nhập
                logger.info("Processing order for logged-in user: {}", username);
                customer = customerRepository.findByUsername(username)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Không tìm thấy Customer với username: " + username));
                
                // Chỉ cập nhật các thông tin được chọn
                boolean customerInfoUpdated = false;
                
                if (form.isUpdateName()) {
                    customer.setName(form.getFullName());
                    customerInfoUpdated = true;
                    logger.debug("Updated name for logged-in user: {}", form.getFullName());
                }
                
                if (form.isUpdateEmail()) {
                    String email = form.getEmail();
                    // Chuyển đổi chuỗi rỗng thành null để thỏa mãn ràng buộc database
                    email = (email != null && email.trim().isEmpty()) ? null : email;
                    customer.setEmail(email);
                    customerInfoUpdated = true;
                    logger.debug("Updated email for logged-in user: {}", email);
                }
                
                if (form.isUpdatePhone()) {
                    customer.setPhoneNumber(form.getPhone());
                    customerInfoUpdated = true;
                    logger.debug("Updated phone for logged-in user: {}", form.getPhone());
                }
                
                if (form.isUpdateAddress()) {
                    customer.setStreet(form.getAddress());
                    customer.setWard(form.getWard());
                    customer.setDistrict(form.getDistrict());
                    customer.setCity(form.getProvince());
                    customerInfoUpdated = true;
                    logger.debug("Updated address for logged-in user: {}", form.getAddress());
                }
                
                if (customerInfoUpdated) {
                    customer.setUpdateAt(LocalDateTime.now());
                    customerRepository.save(customer);
                    logger.info("Updated customer information based on checkboxes for user: {}", username);
                } else {
                    logger.info("No customer information updates requested for user: {}", username);
                }
            } else {
                // Người dùng chưa đăng nhập
                logger.info("Processing order for guest user with phone: {}", form.getPhone());
                
                if (form.getPhone() == null || form.getPhone().trim().isEmpty()) {
                    logger.warn("Missing required phone number for guest checkout");
                    redirectAttributes.addFlashAttribute("error", "Số điện thoại là bắt buộc");
                    return "redirect:/checkout";
                }
                
                // Tìm khách hàng theo số điện thoại
                Optional<Customer> existingCustomer = customerRepository.findByPhoneNumber(form.getPhone());
                
                if (existingCustomer.isPresent()) {
                    // Đã có tài khoản với số điện thoại này
                    customer = existingCustomer.get();
                    logger.info("Found existing customer with phone: {} (id: {})", form.getPhone(), customer.getCustomerId());
                    
                    // Chỉ cập nhật các thông tin được chọn
                    boolean customerInfoUpdated = false;
                    
                    if (form.isUpdateName()) {
                        customer.setName(form.getFullName());
                        customerInfoUpdated = true;
                        logger.debug("Updated name for existing customer: {}", form.getFullName());
                    }
                    
                    if (form.isUpdateEmail()) {
                        String email = form.getEmail();
                        // Chuyển đổi chuỗi rỗng thành null để thỏa mãn ràng buộc database
                        email = (email != null && email.trim().isEmpty()) ? null : email;
                        customer.setEmail(email);
                        customerInfoUpdated = true;
                        logger.debug("Updated email for existing customer: {}", email);
                    }
                    
                    if (form.isUpdateAddress()) {
                        customer.setStreet(form.getAddress());
                        customer.setWard(form.getWard());
                        customer.setDistrict(form.getDistrict());
                        customer.setCity(form.getProvince());
                        customerInfoUpdated = true;
                        logger.debug("Updated address for existing customer: {}", form.getAddress());
                    }
                    
                    if (customerInfoUpdated) {
                        customer.setUpdateAt(LocalDateTime.now());
                        logger.info("Updated existing customer information based on checkboxes");
                    } else {
                        logger.info("No customer information updates requested for existing customer");
                    }
                } else {
                    // Tạo tài khoản mới theo cách trang đăng ký xử lý
                    logger.info("Creating new customer account for phone: {}", form.getPhone());
                    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                    customer = new Customer();
                    
                    // Username và email có thể là null
                    // Chuyển đổi chuỗi rỗng thành null để thỏa mãn ràng buộc database
                    String email = form.getEmail();
                    email = (email != null && email.trim().isEmpty()) ? null : email;
                    
                    customer.setUsername(null); // Không cần username
                    customer.setPassword(passwordEncoder.encode("123")); // Đặt mật khẩu cố định là "123"
                    customer.setName(form.getFullName());
                    customer.setPhoneNumber(form.getPhone());
                    customer.setEmail(email);
                    customer.setStreet(form.getAddress());
                    customer.setWard(form.getWard());
                    customer.setDistrict(form.getDistrict());
                    customer.setCity(form.getProvince());
                    customer.setCreateAt(LocalDateTime.now());
                    customer.setStatus("active");
                    customer.setLoyaltyPoints(0);
                    customer.setRating(0);
                    customer.setNote("automatically created");
                }
                
                customer = customerRepository.save(customer);
                logger.info("Saved customer: {}", customer.getCustomerId());
            }

            // Lấy giỏ hàng
            List<CartItemDTO> cartItems = cartService.getCartItems(username, sessionId);
            
            if (cartItems.isEmpty()) {
                logger.warn("Attempted to place order with empty cart");
                redirectAttributes.addFlashAttribute("error", "Giỏ hàng của bạn đang trống");
                return "redirect:/cart";
            }
            
            BigDecimal totalAmount = cartItems.stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            logger.info("Creating order with total amount: {}", totalAmount);
            
            // Lấy Employee có id=1 để gán cho đơn hàng
            Employee defaultEmployee;
            try {
                defaultEmployee = employeeRepository.findById(1)
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy Employee với id=1"));
                logger.info("Assigning default employee with id={} to order", defaultEmployee.getEmployeeId());
            } catch (Exception e) {
                logger.error("Failed to find default employee (id=1): {}", e.getMessage());
                redirectAttributes.addFlashAttribute("error", "Không thể tạo đơn hàng do lỗi hệ thống. Vui lòng thử lại sau.");
                return "redirect:/checkout";
            }
            
            // Tạo đơn hàng với Employee có id=1
            Order order = new Order();
            order.setCustomer(customer);
            order.setEmployee(defaultEmployee); // Gán Employee id=1 cho cả hai trường hợp
            order.setShippingAddress(form.getAddress() + ", " + form.getWard() + ", " + form.getDistrict() + ", " + form.getProvince());
            order.setShippingOption(form.getPaymentMethod());
            order.setShippingFee(BigDecimal.valueOf(totalAmount.intValue() > 1000000 ? 0 : 30000));
            order.setTotalAmount(totalAmount);
            order.setOrderStatus("pending"); // Trạng thái ban đầu của đơn hàng
            
            try {
                logger.info("Saving order to database...");
                order = orderRepository.save(order);
                logger.info("Created order: {}", order.getOrderId());
            } catch (Exception e) {
                logger.error("Error saving order to database: {}", e.getMessage(), e);
                redirectAttributes.addFlashAttribute("error", "Không thể lưu đơn hàng. Lỗi: " + e.getMessage());
                return "redirect:/checkout";
            }

            // Chuẩn bị danh sách sản phẩm cho email
            List<Map<String, Object>> orderItems = new ArrayList<>();
            
            // Lưu từng sản phẩm trong giỏ hàng
            try {
                for (CartItemDTO item : cartItems) {
                    OrderItemDTO orderItem = new OrderItemDTO();
                    orderItem.setOrderId(order.getOrderId());
                    orderItem.setProductVariantId(item.getProductVariantId());
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setUnitPrice(item.getUnitPrice());
                    orderItemService.save(orderItem);
                    logger.debug("Saved order item: variant={}, quantity={}, price={}", 
                        item.getProductVariantId(), item.getQuantity(), item.getUnitPrice());
                    
                    // Lấy thông tin sản phẩm cho email
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("unitPrice", item.getUnitPrice());
                    itemMap.put("total", item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))); 
                    
                    ProductVariantDTO variant = productVariantService.getProductVariantById(item.getProductVariantId());
                    if (variant != null) {
                        itemMap.put("volume", variant.getVolume());
                        
                        ProductDTO product = productService.getProductById(variant.getProductId());
                        itemMap.put("productName", product.getProductName());
                    }
                    
                    orderItems.add(itemMap);
                }
                logger.info("Successfully saved {} order items and updated inventory", orderItems.size());
            } catch (Exception e) {
                logger.error("Error saving order items or updating inventory: {}", e.getMessage(), e);
                // Đơn hàng đã được tạo, nên không redirect mà tiếp tục xử lý
                // Tuy nhiên, vẫn log lỗi để debug
            }
            
            // Tính toán giá trị đơn hàng
            int subtotal = orderItems.stream()
                    .mapToInt(item -> ((BigDecimal) item.get("total")).intValue())
                    .sum();
            int shipping = subtotal > 1000000 ? 0 : 30000;
            int total = subtotal + shipping;

            // Gửi email xác nhận nếu có email
            if (form.getEmail() != null && !form.getEmail().trim().isEmpty()) {
                try {
                    logger.info("Sending order confirmation email to: {}", form.getEmail());
                    emailService.sendOrderConfirmationEmail(
                        form.getEmail(), 
                        form.getFullName(), 
                        order, 
                        orderItems,
                        subtotal,
                        shipping,
                        total
                    );
                    logger.info("Order confirmation email sent successfully");
                } catch (Exception e) {
                    logger.error("Failed to send order confirmation email: {}", e.getMessage(), e);
                    // Không làm gián đoạn quy trình thanh toán nếu gửi email thất bại
                }
            } else {
                logger.info("No email provided, skipping order confirmation email");
            }

            // Xoá giỏ hàng sau khi đặt hàng
            try {
                for (CartItemDTO item : cartItems) {
                    cartService.removeCartItem(item.getCartItemId(), username, sessionId);
                }
                logger.info("Successfully cleared the cart after order completion");
                
                // Cập nhật số lượng giỏ hàng trong session thành 0
                updateCartItemCountInSession(request);
            } catch (Exception e) {
                logger.error("Error clearing cart after order: {}", e.getMessage(), e);
                // Không làm gián đoạn quy trình thanh toán nếu xóa giỏ hàng thất bại
            }

            logger.info("Order processing completed successfully for order: {}", order.getOrderId());
            // Thêm thông báo thành công vào redirectAttributes
            redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công! Mã đơn hàng của bạn là #" + order.getOrderId());
            return "redirect:/order-success?orderId=" + order.getOrderId();
            
        } catch (Exception e) {
            logger.error("Error processing order: ", e);
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi xử lý đơn hàng: " + e.getMessage());
            return "redirect:/checkout";
        }
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
        HttpSession session = request.getSession(true); // true để tạo session nếu chưa có
        String sessionId = (String) session.getAttribute("CART_SESSION_ID");
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            session.setAttribute("CART_SESSION_ID", sessionId);
        }
        return sessionId;
    }

    private void updateCartItemCountInSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("cartItemCount", 0);
    }
} 