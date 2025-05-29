package com.hyperformancelabs.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CustomerPaymentMethod", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_CustomerPaymentMethod", columnNames = {"customer_id", "payment_method_id", "account_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_payment_method_id")
    private Integer customerPaymentMethodId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "token", length = 255)
    private String token;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;
}
