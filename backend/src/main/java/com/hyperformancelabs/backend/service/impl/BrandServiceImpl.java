package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.BrandDTO;
import com.hyperformancelabs.backend.model.Brand;
import com.hyperformancelabs.backend.repository.BrandRepository;
import com.hyperformancelabs.backend.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Override
    public List<BrandDTO> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::convertToBrandDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BrandDTO getBrandById(Integer brandId) {
        return brandRepository.findById(brandId)
                .map(this::convertToBrandDTO)
                .orElse(null);
    }

    @Override
    public BrandDTO getBrandByBrandName(String brandName) {
        return brandRepository.findByBrandName(brandName)
                .map(this::convertToBrandDTO)
                .orElse(null);
    }

    private BrandDTO convertToBrandDTO(Brand brand) {
        return new BrandDTO(
                brand.getBrandId(),
                brand.getBrandName(),
                brand.getBrandDescription(),
                brand.getCountryOfOrigin(),
                brand.getLogoUrl(),
                brand.getWebsiteUrl()
        );
    }
}
