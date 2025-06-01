package com.hyperformancelabs.backend.controller.user;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            logger.error("Error occurred with status code: {} and message: {}", statusCode, errorMessage);
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorMessage", errorMessage != null ? errorMessage : "Không tìm thấy trang bạn yêu cầu");
                return "error/404";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("errorMessage", errorMessage != null ? errorMessage : "Đã xảy ra lỗi từ phía máy chủ");
                return "error/500";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("errorMessage", "Bạn không có quyền truy cập trang này");
                return "error/error";
            }
        }
        
        model.addAttribute("errorMessage", errorMessage != null ? errorMessage : "Đã xảy ra lỗi không mong muốn");
        return "error/error";
    }
} 