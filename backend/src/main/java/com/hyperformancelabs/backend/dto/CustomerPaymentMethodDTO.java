package com.hyperformancelabs.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPaymentMethodDTO {

    private Integer customerPaymentMethodId;

    private Integer customerId;

    private Integer paymentMethodId;

    private String provider;

    private String accountNumber;

    private String token;

    private Boolean isDefault;
}
