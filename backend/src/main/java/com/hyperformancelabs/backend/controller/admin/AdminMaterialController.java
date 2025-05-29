package com.hyperformancelabs.backend.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/materials")
public class AdminMaterialController {

    @GetMapping
    public String listMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String search,
            Model model) {
        
        // In a real application, this would use materialService.getMaterials(page, search)
        // For now, we'll use sample data
        
        List<Map<String, Object>> materials = new ArrayList<>();
        
        // Sample material data
        materials.add(createSampleMaterial(1, "Hộp đựng nước hoa 50ml", "Bao bì", 150, 50, BigDecimal.valueOf(15000)));
        materials.add(createSampleMaterial(2, "Hộp đựng nước hoa 100ml", "Bao bì", 120, 40, BigDecimal.valueOf(20000)));
        materials.add(createSampleMaterial(3, "Túi giấy cao cấp", "Bao bì", 200, 100, BigDecimal.valueOf(12000)));
        materials.add(createSampleMaterial(4, "Ruy băng", "Trang trí", 300, 100, BigDecimal.valueOf(5000)));
        materials.add(createSampleMaterial(5, "Giấy gói quà", "Trang trí", 250, 80, BigDecimal.valueOf(8000)));
        
        model.addAttribute("materials", materials);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 2); // Sample value
        model.addAttribute("search", search);
        
        return "admin/materials/list";
    }
    
    @GetMapping("/{id}")
    public String viewMaterial(@PathVariable("id") Integer id, Model model) {
        // In a real application, this would use materialService.getMaterialById(id)
        // For now, we'll use sample data
        
        Map<String, Object> material = createSampleMaterial(id, "Hộp đựng nước hoa 50ml", "Bao bì", 150, 50, BigDecimal.valueOf(15000));
        material.put("description", "Hộp đựng nước hoa cao cấp, màu đen, kích thước 10x10x15cm");
        
        model.addAttribute("material", material);
        
        // Sample transaction history
        List<Map<String, Object>> transactions = new ArrayList<>();
        transactions.add(Map.of(
            "id", 1001,
            "type", "purchase",
            "quantity", 50,
            "date", "12/04/2025",
            "employee", "Admin User"
        ));
        transactions.add(Map.of(
            "id", 1002,
            "type", "use",
            "quantity", 10,
            "date", "15/04/2025",
            "employee", "Staff User 1"
        ));
        
        model.addAttribute("transactions", transactions);
        
        return "admin/materials/detail";
    }
    
    private Map<String, Object> createSampleMaterial(int id, String name, String category, int stock, int reorderLevel, BigDecimal price) {
        Map<String, Object> material = new HashMap<>();
        material.put("id", id);
        material.put("name", name);
        material.put("category", category);
        material.put("quantityInStock", stock);
        material.put("reorderLevel", reorderLevel);
        material.put("price", price);
        return material;
    }
}
