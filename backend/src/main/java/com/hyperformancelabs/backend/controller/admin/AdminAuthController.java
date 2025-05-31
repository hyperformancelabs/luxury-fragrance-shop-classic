package com.hyperformancelabs.backend.controller.admin;

import com.hyperformancelabs.backend.dto.admin.common.EmployeeDTO;
import com.hyperformancelabs.backend.model.Employee;
import com.hyperformancelabs.backend.repository.EmployeeRepository;
import com.hyperformancelabs.backend.service.AdminPasswordResetService;
import com.hyperformancelabs.backend.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@Validated
public class AdminAuthController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AdminPasswordResetService adminPasswordResetService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService adminDetailsService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/login")
    public String showLoginForm() {
        return "admin/login";
    }

    @PostMapping("/precheck")
    public String loginAdmin(@RequestParam String usernameEmailOrPhone,
                             @RequestParam String password,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        System.out.println("=== ADMIN LOGIN DEBUG ===");
        System.out.println("Login attempt with usernameEmailOrPhone: " + usernameEmailOrPhone);
        
        EmployeeDTO employee = employeeService.findActiveSystemAdminByUsernameEmailOrPhone(usernameEmailOrPhone);
        if (employee == null) {
            System.out.println("Employee not found for: " + usernameEmailOrPhone);
            redirectAttributes.addFlashAttribute("error", "Tài khoản admin không tồn tại hoặc không có quyền.");
            return "redirect:/admin/login";
        }

        System.out.println("Found employee: " + employee.getUsername() + " (ID: " + employee.getEmployeeId() + ")");

        if (!passwordEncoder.matches(password, employee.getPassword())) {
            System.out.println("Password mismatch for user: " + employee.getUsername());
            redirectAttributes.addFlashAttribute("error", "Mật khẩu không đúng.");
            return "redirect:/admin/login";
        }

        System.out.println("Password validated. Loading UserDetails for username: " + employee.getUsername());
        UserDetails userDetails = adminDetailsService.loadUserByUsername(employee.getUsername());

        System.out.println("UserDetails loaded: " + userDetails.getUsername());
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        // Truy vấn và lưu role vào session
        List<String> roles = employeeService.findActiveRoleNamesByEmployeeId(employee.getEmployeeId());
        request.getSession().setAttribute("ROLES", roles);
        
        System.out.println("Authentication set successfully. Roles: " + roles);
        System.out.println("=== END ADMIN LOGIN DEBUG ===");

        return "redirect:/admin/dashboard";
    }

    /**
     * Show forgot password form for admin
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        return "admin/auth/forgot-password";
    }

    /**
     * Process forgot password request for admin
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam @NotBlank @Email String email,
                                        RedirectAttributes redirectAttributes) {
        try {
            boolean result = adminPasswordResetService.createPasswordResetToken(email);
            
            if (result) {
                redirectAttributes.addFlashAttribute("success", 
                    "Nếu email này tồn tại trong hệ thống, bạn sẽ nhận được mã xác thực để đặt lại mật khẩu.");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Có lỗi xảy ra. Vui lòng thử lại sau.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra. Vui lòng thử lại sau.");
        }

        return "redirect:/admin/forgot-password";
    }

    /**
     * Show reset password form with token for admin
     */
    @GetMapping("/reset-password-with-token")
    public String showResetPasswordForm(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.trim().isEmpty()) {
            model.addAttribute("error", "Token không hợp lệ.");
            return "admin/auth/reset-password-with-token";
        }

        // Validate token
        Employee employee = adminPasswordResetService.validateToken(token);
        if (employee == null) {
            model.addAttribute("error", "Token không hợp lệ hoặc đã hết hạn.");
            return "admin/auth/reset-password-with-token";
        }

        model.addAttribute("token", token);
        model.addAttribute("employeeName", employee.getFullName());
        return "admin/auth/reset-password-with-token";
    }

    /**
     * Process password reset with token for admin
     */
    @PostMapping("/reset-password-with-token")
    public String processPasswordReset(@RequestParam String token,
                                       @RequestParam @NotBlank @Size(min = 6, max = 100) String newPassword,
                                       @RequestParam @NotBlank String confirmPassword,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        
        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu không khớp.");
            model.addAttribute("token", token);
            return "admin/auth/reset-password-with-token";
        }

        // Validate token again
        Employee employee = adminPasswordResetService.validateToken(token);
        if (employee == null) {
            model.addAttribute("error", "Token không hợp lệ hoặc đã hết hạn.");
            return "admin/auth/reset-password-with-token";
        }

        // Reset password
        boolean result = adminPasswordResetService.resetPassword(token, newPassword);
        if (result) {
            redirectAttributes.addFlashAttribute("success", 
                "Mật khẩu đã được đặt lại thành công. Vui lòng đăng nhập với mật khẩu mới.");
            return "redirect:/admin/login";
        } else {
            model.addAttribute("error", "Có lỗi xảy ra khi đặt lại mật khẩu. Vui lòng thử lại.");
            model.addAttribute("token", token);
            model.addAttribute("employeeName", employee.getFullName());
            return "admin/auth/reset-password-with-token";
        }
    }
}
