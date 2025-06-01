package com.hyperformancelabs.backend.exception;

import com.hyperformancelabs.backend.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, 
                                                RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/error";
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolationException(DataIntegrityViolationException ex,
                                                       RedirectAttributes redirectAttributes) {
        String errorMessage = "Đã xảy ra lỗi khi lưu dữ liệu. Vui lòng kiểm tra lại thông tin.";
        
        // Extract more specific error messages if possible
        if (ex.getMessage().contains("UQ__Customer") || ex.getMessage().contains("unique constraint")) {
            errorMessage = "Thông tin đã tồn tại trong hệ thống. Vui lòng thử với thông tin khác.";
        }
        
        redirectAttributes.addFlashAttribute("error", errorMessage);
        return "redirect:/error";
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleAllUncaughtException(Exception exception) {
        ModelAndView modelAndView = new ModelAndView("error/500");
        modelAndView.addObject("errorMessage", "Đã xảy ra lỗi không mong muốn: " + exception.getMessage());
        return modelAndView;
    }
}