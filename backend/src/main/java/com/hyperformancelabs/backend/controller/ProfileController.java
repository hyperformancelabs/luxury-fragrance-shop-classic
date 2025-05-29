package com.hyperformancelabs.backend.controller;

import com.hyperformancelabs.backend.dto.CustomerDTO;
import com.hyperformancelabs.backend.dto.OrderDTO;
import com.hyperformancelabs.backend.model.Customer;
import com.hyperformancelabs.backend.model.Order;
import com.hyperformancelabs.backend.service.CustomerService;
import com.hyperformancelabs.backend.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String viewProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        CustomerDTO customer = customerService.getCustomerByUsername(username);
        model.addAttribute("user", customer);
        return "profile/profile";
    }

    @GetMapping("/orders")
    public String viewOrders(Model model, Authentication authentication) {
        String username = authentication.getName();
        CustomerDTO customer = customerService.getCustomerByUsername(username);
        List<OrderDTO> orders = orderService.findOrdersByCustomerId(customer.getCustomerId());
        model.addAttribute("orders", orders);
        model.addAttribute("user", customer);
        return "profile/orders";
    }

    @GetMapping("/edit")
    public String editProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        CustomerDTO customer = customerService.getCustomerByUsername(username);
        model.addAttribute("user", customer);
        return "profile/edit-profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam String username,
                                @RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String phoneNumber,
                                @RequestParam String street,
                                @RequestParam String ward,
                                @RequestParam String district,
                                @RequestParam String city,
                                @RequestParam(required = false) String currentPassword,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) String confirmPassword,
                                RedirectAttributes redirectAttributes) {

        CustomerDTO customer = customerService.getCustomerByUsername(username);
        if (customer == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng.");
            return "redirect:/profile/edit";
        }

        // Cập nhật thông tin cơ bản
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhoneNumber(phoneNumber);
        customer.setStreet(street);
        customer.setWard(ward);
        customer.setDistrict(district);
        customer.setCity(city);

        // Nếu có đổi mật khẩu
        if (currentPassword != null && !currentPassword.isBlank()
                && newPassword != null && !newPassword.isBlank()
                && confirmPassword != null && !confirmPassword.isBlank()) {

            if (!passwordEncoder.matches(currentPassword, customer.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không chính xác.");
                return "redirect:/profile/edit";
            }

            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Xác nhận mật khẩu mới không khớp.");
                return "redirect:/profile/edit";
            }

            customer.setPassword(passwordEncoder.encode(newPassword));
        }

        customerService.updateCustomer(customer);
        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công.");
        return "redirect:/profile";
    }
}
