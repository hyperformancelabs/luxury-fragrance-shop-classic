package com.hyperformancelabs.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ProductDetail", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ProductDetail", columnNames = {"product_id", "detail_name", "detail_value"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_detail_id")
    private Integer productDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "detail_name", nullable = false, length = 50)
    private String detailName;

    @Column(name = "detail_value", nullable = false, length = 255)
    private String detailValue;

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    public enum DetailName {
        TONE_SCENT, STYLE, TOP_NOTE, MIDDLE_NOTE, BASE_NOTE, LONGEVITY, PROJECTION, SEASON, TIME_OF_DAY, SUITABLE_AGE, SUITABLE_GENDER
    }
}
