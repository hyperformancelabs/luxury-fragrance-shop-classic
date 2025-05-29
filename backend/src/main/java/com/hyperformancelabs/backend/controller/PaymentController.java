package com.hyperformancelabs.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @GetMapping("/vnpay")
    public String vnpayPayment(Model model, @RequestParam(required = false) String orderId) {
        // Trong thực tế, bạn sẽ lấy thông tin đơn hàng từ database dựa trên orderId
        // Ở đây, chúng ta sẽ tạo dữ liệu mẫu
        if (orderId == null || orderId.isEmpty()) {
            orderId = "APH" + System.currentTimeMillis();
        }
        
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", 8000000);
        
        return "payment/vnpay";
    }
    
    @PostMapping("/vnpay/process")
    public String processVnpayPayment() {
        // Trong thực tế, bạn sẽ xử lý thanh toán VNPay ở đây
        // Sau đó chuyển hướng đến trang xác nhận đơn hàng
        return "redirect:/order-success";
    }
    
    @GetMapping("/mbbank")
    public String mbbankPayment(Model model, @RequestParam(required = false) String orderId) {
        // Trong thực tế, bạn sẽ lấy thông tin đơn hàng từ database dựa trên orderId
        // Ở đây, chúng ta sẽ tạo dữ liệu mẫu
        if (orderId == null || orderId.isEmpty()) {
            orderId = "APH" + System.currentTimeMillis();
        }
        
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", 8000000);
        model.addAttribute("accountNumber", "0123456789");
        model.addAttribute("accountName", "CÔNG TY TNHH APH PERFUME");
        model.addAttribute("bankName", "MB BANK");
        
        return "payment/mbbank";
    }
    
    @PostMapping("/mbbank/process")
    public String processMbbankPayment() {
        // Trong thực tế, bạn sẽ xử lý thanh toán MBBank ở đây
        // Sau đó chuyển hướng đến trang xác nhận đơn hàng
        return "redirect:/order-success";
    }
}
