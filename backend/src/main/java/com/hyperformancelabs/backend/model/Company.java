package com.hyperformancelabs.backend.model;

import org.springframework.stereotype.Component;

/**
 * Company bean for dependency injection example
 * This demonstrates how beans can be used across the application
 * as shown in Bài 5: Bean và Dependency Injection
 */
@Component
public class Company {
    private String name;
    private String slogan;
    private String logo;
    private String address;
    private String phone;
    private String email;

    /**
     * Default constructor initializes with default values
     */
    public Company() {
        this.name = "APH Perfume";
        this.slogan = "Khám phá vẻ đẹp tinh tế";
        this.logo = "/images/logo.png";
        this.address = "123 Đường Lê Lợi, Quận 1, TP. Hồ Chí Minh";
        this.phone = "0123456789";
        this.email = "info@aphperfume.com";
    }

    /**
     * Constructor with parameters
     */
    public Company(String name, String slogan, String logo, String address, String phone, String email) {
        this.name = name;
        this.slogan = slogan;
        this.logo = logo;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
} 