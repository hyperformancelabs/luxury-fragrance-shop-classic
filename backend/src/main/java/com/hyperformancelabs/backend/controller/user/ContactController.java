package com.hyperformancelabs.backend.controller.user;

import com.hyperformancelabs.backend.dto.user.request.ContactFormRequest;
import com.hyperformancelabs.backend.service.EmailService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/contact")
public class ContactController {
    
    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    
    @Autowired
    private EmailService emailService;

    @GetMapping
    public String contact(Model model) {
        model.addAttribute("contactForm", new ContactFormRequest());
        return "user/contact/contact";
    }
    
    @PostMapping("/send")
    public String sendContactForm(
            @Valid @ModelAttribute("contactForm") ContactFormRequest contactForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (bindingResult.hasErrors()) {
            // Return to form with validation errors
            logger.warn("Contact form validation failed: {}", bindingResult.getAllErrors());
            return "user/contact/contact";
        }
        
        try {
            // Send email
            emailService.sendContactFormEmail(
                    contactForm.getName(),
                    contactForm.getEmail(),
                    contactForm.getSubject(),
                    contactForm.getMessage()
            );
            
            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Cảm ơn bạn đã liên hệ với chúng tôi! Chúng tôi sẽ phản hồi trong thời gian sớm nhất.");
            
            logger.info("Contact form submitted successfully from: {}", contactForm.getEmail());
            
            return "redirect:/contact?success=true";
            
        } catch (Exception e) {
            // Log error and return to form with error message
            logger.error("Error sending contact form email", e);
            model.addAttribute("errorMessage", 
                    "Đã xảy ra lỗi khi gửi thông tin liên hệ. Vui lòng thử lại sau hoặc liên hệ qua số điện thoại.");
            
            return "user/contact/contact";
        }
    }
} 