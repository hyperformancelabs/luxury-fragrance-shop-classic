package com.hyperformancelabs.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDTO {

    private Integer paymentMethodId;

    private String methodName;

    private String description;
}
