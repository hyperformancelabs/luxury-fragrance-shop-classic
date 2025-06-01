package com.hyperformancelabs.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Entity for storing Terms and Conditions content
 */
@Entity
@Table(name = "terms_and_conditions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermsAndConditions {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Lob
    @Column(name = "markdown_content", columnDefinition = "TEXT")
    private String markdownContent;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_updated")
    private Date lastUpdated;
    
    // Update timestamp before persisting
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.lastUpdated = new Date();
    }
} 