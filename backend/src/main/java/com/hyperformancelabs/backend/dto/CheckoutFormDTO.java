package com.hyperformancelabs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutFormDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String province;
    private String district;
    private String ward;
    private String addressDetail;
    private String paymentMethod;
    private boolean sameAddress;

    // Shipping fields (nếu khác)
    private String shippingFirstName;
    private String shippingLastName;
    private String shippingPhone;
    private String shippingAddress;
    private String shippingProvince;
    private String shippingDistrict;
    private String shippingWard;
    private String shippingAddressDetail;
}
