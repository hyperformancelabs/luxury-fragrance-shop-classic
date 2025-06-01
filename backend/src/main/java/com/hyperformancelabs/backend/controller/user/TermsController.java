package com.hyperformancelabs.backend.controller.user;

import com.hyperformancelabs.backend.dto.common.response.BreadcrumbItemDTO;
import com.hyperformancelabs.backend.model.Company;
import com.hyperformancelabs.backend.model.TermsAndConditions;
import com.hyperformancelabs.backend.service.TermsAndConditionsService;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Arrays;
import java.util.List;

/**
 * Controller for Terms and Conditions page
 * Demonstrates various Spring MVC concepts from the course:
 * - RequestMapping
 * - ModelAttribute
 * - Dependency Injection
 */
@Controller
public class TermsController {

    // Dependency Injection example with Company bean
    private final Company company;
    private final TermsAndConditionsService termsService;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    @Autowired
    public TermsController(Company company, TermsAndConditionsService termsService) {
        this.company = company;
        this.termsService = termsService;
        this.markdownParser = Parser.builder().build();
        this.htmlRenderer = HtmlRenderer.builder().build();
    }

    /**
     * Using @ModelAttribute to add common model attributes
     * that will be available in all handler methods of this controller
     */
    @ModelAttribute("paymentMethods")
    public List<String> getPaymentMethods() {
        return Arrays.asList(
            "Thanh toán khi nhận hàng (COD)",
            "Chuyển khoản ngân hàng",
            "Ví điện tử MoMo",
            "Thẻ tín dụng/Ghi nợ",
            "VnPay"
        );
    }

    /**
     * Main handler for terms and conditions page
     * Using UI Model to pass data to the view
     */
    @GetMapping("/terms-conditions")
    public String termsAndConditions(Model model) {
        // Get terms and conditions from database
        TermsAndConditions terms = termsService.getLatestTerms();
        
        // Convert markdown to HTML
        String markdownContent = terms.getMarkdownContent();
        Node document = markdownParser.parse(markdownContent);
        String htmlContent = htmlRenderer.render(document);
        
        // Add breadcrumb paths
        List<BreadcrumbItemDTO> breadcrumbPaths = List.of(
            BreadcrumbItemDTO.createCurrentPage("ĐIỀU KHOẢN & ĐIỀU KIỆN")
        );
        
        // Add attributes to model
        model.addAttribute("lastUpdated", terms.getLastUpdated());
        model.addAttribute("deliveryTime", 3);
        model.addAttribute("company", company);
        model.addAttribute("showHighlight", true);
        model.addAttribute("htmlContent", htmlContent);
        model.addAttribute("breadcrumbPaths", breadcrumbPaths);
        
        return "user/terms/terms-conditions";
    }
} 