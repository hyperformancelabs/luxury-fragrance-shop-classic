//package com.hyperformancelabs.backend.controller.admin;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@Controller
//@RequestMapping("/admin")
//public class AdminLoginController {
//
//    @GetMapping("/login")
//    public String loginPage() {
//        return "admin/login";
//    }
//
//    @PostMapping("/login")
//    public String processLogin(
//            @RequestParam String username,
//            @RequestParam String password,
//            Model model) {
//
//        // In a real application, this would use authentication service
//        // For now, we'll use a simple check
//
//        if ("admin".equals(username) && "password".equals(password)) {
//            // Successful login
//            return "redirect:/admin/dashboard";
//        } else {
//            // Failed login
//            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
//            return "admin/login";
//        }
//    }
//
//    @GetMapping("/forgot-password")
//    public String forgotPasswordPage() {
//        return "admin/forgot-password";
//    }
//
//    @PostMapping("/forgot-password")
//    public String processForgotPassword(
//            @RequestParam String email,
//            Model model) {
//
//        // In a real application, this would send a password reset email
//        // For now, we'll just show a success message
//
//        model.addAttribute("success", "Hướng dẫn đặt lại mật khẩu đã được gửi đến email của bạn");
//        return "admin/forgot-password";
//    }
//
//    @GetMapping("/logout")
//    public String logout() {
//        // In a real application, this would invalidate the session
//        return "redirect:/admin/login";
//    }
//}
