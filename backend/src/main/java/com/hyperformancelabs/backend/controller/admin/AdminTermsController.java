package com.hyperformancelabs.backend.controller.admin;

import com.hyperformancelabs.backend.model.TermsAndConditions;
import com.hyperformancelabs.backend.service.TermsAndConditionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for managing Terms and Conditions in admin area
 */
@Controller
@RequestMapping("/admin/terms")
public class AdminTermsController {
    
    private final TermsAndConditionsService termsService;
    
    @Autowired
    public AdminTermsController(TermsAndConditionsService termsService) {
        this.termsService = termsService;
    }
    
    /**
     * Display the terms and conditions editor
     */
    @GetMapping
    public String termsEditor(Model model) {
        TermsAndConditions terms = termsService.getLatestTerms();
        model.addAttribute("markdownContent", terms.getMarkdownContent());
        model.addAttribute("lastUpdated", terms.getLastUpdated());
        model.addAttribute("activeMenu", "terms");
        return "admin/terms/edit";
    }
    
    /**
     * Update terms and conditions
     */
    @PostMapping
    public String updateTerms(@RequestParam("markdownContent") String markdownContent, 
                              RedirectAttributes redirectAttributes) {
        termsService.updateTerms(markdownContent);
        redirectAttributes.addFlashAttribute("success", "Điều khoản và điều kiện đã được cập nhật thành công!");
        return "redirect:/admin/terms";
    }
} 