package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.BrandDTO;

import java.util.List;

public interface BrandService {

    // Lấy tất cả thương hiệu
    List<BrandDTO> getAllBrands();

    // Lấy thương hiệu theo ID
    BrandDTO getBrandById(Integer brandId);

    BrandDTO getBrandByBrandName(String brandName);
}
