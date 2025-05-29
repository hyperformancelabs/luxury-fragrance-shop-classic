package com.hyperformancelabs.backend.controller.admin;

import com.hyperformancelabs.backend.dto.EmployeeDTO;
import com.hyperformancelabs.backend.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService adminDetailsService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "admin/login";
    }

    @PostMapping("/precheck")
    public String loginAdmin(@RequestParam String username,
                             @RequestParam String password,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        EmployeeDTO employee = employeeService.findActiveSystemAdminByEmailOrPhone(username);
        if (employee == null) {
            System.out.println("Tài khoản admin không tồn tại hoặc không có quyền.");
            redirectAttributes.addFlashAttribute("error", "Tài khoản admin không tồn tại hoặc không có quyền.");
            return "redirect:/admin/login";
        }

        String passwordHash = passwordEncoder.encode(employee.getPassword());
        if (!passwordEncoder.matches(password, passwordHash)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu không đúng.");
            return "redirect:/admin/login";
        }

        UserDetails userDetails = adminDetailsService.loadUserByUsername(employee.getEmail() != null
                ? employee.getEmail() : employee.getPhoneNumber());

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        // Truy vấn và lưu role vào session
        List<String> roles = employeeService.findActiveRoleNamesByEmployeeId(employee.getEmployeeId());
        request.getSession().setAttribute("ROLES", roles);

        return "redirect:/admin/dashboard";
    }
}
