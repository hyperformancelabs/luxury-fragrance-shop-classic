package com.hyperformancelabs.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "MaterialTransaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_transaction_id")
    private Integer materialTransactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private Employee performedBy;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "before_quantity")
    private Integer beforeQuantity;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "after_quantity")
    private Integer afterQuantity;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType;

    @Column(name = "reason", columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;
}
