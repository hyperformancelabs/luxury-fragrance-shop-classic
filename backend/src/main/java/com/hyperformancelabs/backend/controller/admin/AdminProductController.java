package com.hyperformancelabs.backend.controller.admin;

import com.hyperformancelabs.backend.dto.*;
import com.hyperformancelabs.backend.repository.BrandRepository;
import com.hyperformancelabs.backend.service.BrandService;
import com.hyperformancelabs.backend.service.ProductDetailService;
import com.hyperformancelabs.backend.service.ProductService;
import com.hyperformancelabs.backend.service.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private ProductDetailService productDetailService;

    @GetMapping
    public String listProducts(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) String gender,
                               @RequestParam(required = false) String stockStatus,
                               @RequestParam(required = false) String sortByAndDir,
                               Model model) {
        // Lấy dánh sách thương hiệu
        List<BrandDTO> brands = brandService.getAllBrands();

        // Loại bỏ các chuỗi rỗng
        search = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        gender = (gender != null && !gender.trim().isEmpty()) ? gender.trim() : null;
        stockStatus = (stockStatus != null && !stockStatus.trim().isEmpty()) ? stockStatus.trim() : null;
        sortByAndDir = (sortByAndDir != null && !sortByAndDir.trim().isEmpty()) ? sortByAndDir.trim() : null;

        String sortBy = null;
        String sortDir = null;

        if (sortByAndDir != null && sortByAndDir.contains("-")) {
            String[] parts = sortByAndDir.split("-");
            sortBy = parts[0];
            sortDir = parts[1];
        }

        Page<ProductAdminDisplayDTO> productsPage = productService.filterAdminProductsPaged(
                gender, search, sortBy, sortDir, stockStatus, PageRequest.of(page, 10)
        );

        // Tạo queryParams chỉ giữ param có giá trị
        StringBuilder queryParams = new StringBuilder();
        if (search != null) queryParams.append("&search=").append(search);
        if (gender != null) queryParams.append("&gender=").append(gender);
        if (stockStatus != null) queryParams.append("&stockStatus=").append(stockStatus);
        if (sortBy != null && sortDir != null) queryParams.append("&sortByAndDir=").append(sortBy).append("-").append(sortDir);

        model.addAttribute("queryParams", queryParams.toString());
        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("search", search);
        model.addAttribute("gender", gender);
        model.addAttribute("stockStatus", stockStatus);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalItems", productsPage.getTotalElements());
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("brands", brands);

        return "admin/products/list";
    }

    @Transactional
    @PostMapping("/add")
    public String addProduct(@RequestParam("product_name") String productName,
                             @RequestParam("brand_id") Integer brandId,
                             @RequestParam("description") String description,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             @RequestParam("volume") Integer volume,
                             @RequestParam("price") BigDecimal price,
                             @RequestParam(name = "discount_price", required = false) BigDecimal discountPrice,
                             @RequestParam("quantity_in_stock") Integer quantityInStock,
                             @RequestParam(name = "reorder_level", required = false) Integer reorderLevel,
                             @RequestParam("gender") String gender,
                             RedirectAttributes redirectAttributes) {
        try {
            // 1. Lưu ảnh
            String imageUrl = saveImageFile(imageFile);

            // 2. Tạo Product
            ProductDTO product = new ProductDTO();
            product.setProductName(productName);
            product.setBrandName(brandService.getBrandById(brandId).getBrandName());
            product.setDescription(description);
            product.setImageUrl(imageUrl);
            ProductDTO addedProduct = productService.addProduct(product);

            // 3. Tạo ProductVariant đầu tiên
            ProductVariantDTO variant = new ProductVariantDTO();
            variant.setProductId(addedProduct.getProductId());
            variant.setVolume(volume);
            variant.setPrice(price);
            variant.setDiscountPrice(discountPrice);
            variant.setQuantityInStock(quantityInStock);
            variant.setReorderLevel(reorderLevel);
            productVariantService.addProductVariant(variant);

            // 4. Cập nhật gender cho product
            ProductDetailDTO detail = new ProductDetailDTO();
            detail.setProductId(addedProduct.getProductId());
            detail.setDetailName("suitable_gender");
            detail.setDetailValue(gender);
            productDetailService.addProductDetail(detail);

            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công!");
        } catch (Exception e) {
            e.printStackTrace(); // ✅ Ghi log để debug
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi thêm sản phẩm: " + e.getMessage());
            throw new RuntimeException("Thêm sản phẩm thất bại", e); // ✅ Đảm bảo rollback
        }

        return "redirect:/admin/products";
    }

    @Transactional
    @PostMapping("/edit")
    public String editProduct(@RequestParam("product_id") Integer productId,
                              @RequestParam("product_name") String productName,
                              @RequestParam("brand_id") Integer brandId,
                              @RequestParam("description") String description,
                              @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                              @RequestParam("old_volume") Integer oldVolume,
                              @RequestParam("volume") Integer newVolume,
                              @RequestParam("price") BigDecimal price,
                              @RequestParam(name = "discount_price", required = false) BigDecimal discountPrice,
                              @RequestParam("quantity_in_stock") Integer quantityInStock,
                              @RequestParam(name = "reorder_level", required = false) Integer reorderLevel,
                              @RequestParam("gender") String gender,
                              @RequestParam(name = "old_gender", required = false) String oldGender,
                              RedirectAttributes redirectAttributes) {
        try {
            // 1. Lấy sản phẩm hiện tại
            ProductDTO product = productService.getProductById(productId);
            if (product == null) throw new IllegalArgumentException("Sản phẩm không tồn tại");

            // 2. Cập nhật thông tin cơ bản
            product.setProductName(productName);
            product.setBrandName(brandService.getBrandById(brandId).getBrandName());
            product.setDescription(description);

            // 3. Cập nhật ảnh nếu có
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = saveImageFile(imageFile);
                product.setImageUrl(imageUrl);
            }

            ProductDTO updatedProduct = productService.updateProduct(product);

            // 4. Cập nhật biến thể (chỉ lấy 1 biến thể đầu tiên để chỉnh sửa)
            ProductVariantDTO variant = productVariantService.findByProduct_ProductIdAndVolume(productId, oldVolume);
            if (variant == null) throw new IllegalArgumentException("Không tìm thấy biến thể sản phẩm");

            variant.setVolume(newVolume);
            variant.setPrice(price);
            variant.setDiscountPrice(discountPrice);
            variant.setQuantityInStock(quantityInStock);
            variant.setReorderLevel(reorderLevel);
            productVariantService.updateProductVariant(variant);

            // 5. Cập nhật giới tính (detail)
            ProductDetailDTO detail = new ProductDetailDTO();
            detail.setProductId(updatedProduct.getProductId());
            detail.setDetailName("suitable_gender");
            detail.setDetailValue(gender);
            productDetailService.saveOrUpdateProductDetail(detail);

            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            throw new RuntimeException("Cập nhật sản phẩm thất bại", e);
        }

        return "redirect:/admin/products";
    }

    @Transactional
    @PostMapping("/delete")
    public String deleteProduct(@RequestParam("productId") Integer productId,
                                RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(productId);
            redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa sản phẩm: " + e.getMessage());
            throw new RuntimeException("Xóa sản phẩm thất bại", e);
        }

        return "redirect:/admin/products";
    }

    private String saveImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IOException("File ảnh trống!");

        // Tạo tên file ngẫu nhiên
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String uploadDir = "uploads/product-images/";

        // Tạo thư mục nếu chưa tồn tại
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        // Lưu file
        Path filePath = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về URL (tuỳ thuộc vào config thực tế)
        return "/" + uploadDir + fileName; // giả sử server mapping /uploads/**
    }

}
