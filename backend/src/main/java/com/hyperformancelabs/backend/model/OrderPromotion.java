package com.hyperformancelabs.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "OrderPromotion", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_OrderPromotion", columnNames = {"order_id", "promotion_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_promotion_id")
    private Integer orderPromotionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;
}
