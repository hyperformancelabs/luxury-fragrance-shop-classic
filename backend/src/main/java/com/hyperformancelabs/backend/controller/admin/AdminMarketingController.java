package com.hyperformancelabs.backend.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/marketing")
public class AdminMarketingController {

    @GetMapping
    public String listCampaigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String status,
            Model model) {
        
        // In a real application, this would use marketingService.getCampaigns(page, status)
        // For now, we'll use sample data
        
        List<Map<String, Object>> campaigns = new ArrayList<>();
        
        // Sample campaign data
        LocalDate today = LocalDate.now();
        campaigns.add(createSampleCampaign(1, "Khuyến mãi mùa hè", today.plusDays(15), today.plusDays(30), "upcoming"));
        campaigns.add(createSampleCampaign(2, "Giảm giá cuối tuần", today.plusDays(2), today.plusDays(4), "upcoming"));
        campaigns.add(createSampleCampaign(3, "Khuyến mãi tháng 4", today.minusDays(15), today.plusDays(5), "active"));
        campaigns.add(createSampleCampaign(4, "Khuyến mãi tháng 3", today.minusDays(45), today.minusDays(15), "completed"));
        campaigns.add(createSampleCampaign(5, "Khuyến mãi tết", today.minusDays(90), today.minusDays(60), "completed"));
        
        model.addAttribute("campaigns", campaigns);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 2); // Sample value
        model.addAttribute("status", status);
        
        return "admin/marketing/list";
    }
    
    @GetMapping("/{id}")
    public String viewCampaign(@PathVariable("id") Integer id, Model model) {
        // In a real application, this would use marketingService.getCampaignById(id)
        // For now, we'll use sample data
        
        LocalDate today = LocalDate.now();
        Map<String, Object> campaign = createSampleCampaign(id, "Khuyến mãi mùa hè", today.plusDays(15), today.plusDays(30), "upcoming");
        campaign.put("description", "Chương trình khuyến mãi mùa hè với nhiều ưu đãi hấp dẫn");
        campaign.put("budget", 10000000);
        campaign.put("targetAudience", "Khách hàng từ 18-35 tuổi");
        
        model.addAttribute("campaign", campaign);
        
        // Sample promotions in this campaign
        List<Map<String, Object>> promotions = new ArrayList<>();
        promotions.add(Map.of(
            "id", 101,
            "name", "Giảm 10% cho nước hoa nam",
            "type", "discount",
            "value", "10%",
            "products", "Tất cả nước hoa nam"
        ));
        promotions.add(Map.of(
            "id", 102,
            "name", "Mua 1 tặng 1 cho nước hoa mini",
            "type", "buy_one_get_one",
            "value", "100%",
            "products", "Nước hoa mini (10ml)"
        ));
        
        model.addAttribute("promotions", promotions);
        
        return "admin/marketing/detail";
    }
    
    private Map<String, Object> createSampleCampaign(int id, String name, LocalDate startDate, LocalDate endDate, String status) {
        Map<String, Object> campaign = new HashMap<>();
        campaign.put("id", id);
        campaign.put("name", name);
        campaign.put("startDate", startDate);
        campaign.put("endDate", endDate);
        campaign.put("status", status);
        return campaign;
    }
}
