package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.model.TermsAndConditions;
import java.util.Date;

public interface TermsAndConditionsService {
    
    /**
     * Get the latest Terms and Conditions
     */
    TermsAndConditions getLatestTerms();
    
    /**
     * Update Terms and Conditions content
     */
    TermsAndConditions updateTerms(String markdownContent);
    
    /**
     * Get the last updated date
     */
    Date getLastUpdatedDate();
} 