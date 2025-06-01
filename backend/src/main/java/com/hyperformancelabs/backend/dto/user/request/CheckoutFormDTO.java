package com.hyperformancelabs.backend.dto.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutFormDTO {
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String province;
    private String district;
    private String ward;
    private String paymentMethod;
    
    // Các checkbox cho phép cập nhật thông tin
    private boolean updateName;
    private boolean updateEmail;
    private boolean updatePhone;
    private boolean updateAddress;
    
    // For backward compatibility with existing code
    public String getFirstName() {
        if (fullName != null && fullName.contains(" ")) {
            return fullName.substring(0, fullName.indexOf(" "));
        }
        return fullName;
    }
    
    public String getLastName() {
        if (fullName != null && fullName.contains(" ")) {
            return fullName.substring(fullName.indexOf(" ") + 1);
        }
        return "";
    }
} 