package com.hyperformancelabs.backend.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalModelAttributeConfig {

    @ModelAttribute("roles")
    public List<String> globalRoles(HttpSession session) {
        List<String> roles = (List<String>) session.getAttribute("ROLES");
        return roles != null ? roles : new ArrayList<>(); // đảm bảo không null
    }
}
