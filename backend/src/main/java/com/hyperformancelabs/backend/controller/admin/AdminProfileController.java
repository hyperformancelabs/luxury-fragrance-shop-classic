package com.hyperformancelabs.backend.controller.admin;

import com.hyperformancelabs.backend.dto.admin.common.EmployeeDTO;
import com.hyperformancelabs.backend.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.net.URL;
import java.net.MalformedURLException;

@Controller
@RequestMapping("/admin")
@Validated
public class AdminProfileController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String UPLOAD_DIR = "uploads/profile-images/";

    /**
     * Show admin profile page
     */
    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication, HttpServletRequest request) {
        try {
            System.out.println("=== AdminProfileController.showProfile ===");
            System.out.println("Authentication: " + authentication);
            System.out.println("Authentication name: " + (authentication != null ? authentication.getName() : "null"));
            
            String username = authentication.getName();
            System.out.println("Getting employee by username: " + username);
            
            EmployeeDTO employee = employeeService.getEmployeeByUsername(username);
            
            if (employee == null) {
                System.out.println("Employee not found for username: " + username);
                return "redirect:/admin/login?error=session_expired";
            }

            System.out.println("Found employee: " + employee.getUsername() + " (ID: " + employee.getEmployeeId() + ")");
            
            // Store profile picture URL in session for header display
            request.getSession().setAttribute("profilePictureUrl", employee.getProfilePictureUrl());
            
            model.addAttribute("employee", employee);
            model.addAttribute("pageTitle", "Hồ sơ cá nhân");
            model.addAttribute("activeMenu", "profile");
            
            System.out.println("Profile page data loaded successfully");
            System.out.println("=== END AdminProfileController.showProfile ===");
            
            return "admin/profile/profile";
            
        } catch (Exception e) {
            System.out.println("Exception in showProfile: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải thông tin profile.");
            return "admin/dashboard";
        }
    }

    /**
     * Show edit profile form
     */
    @GetMapping("/profile/edit")
    public String showEditProfile(Model model, Authentication authentication, HttpServletRequest request) {
        try {
            String username = authentication.getName();
            EmployeeDTO employee = employeeService.getEmployeeByUsername(username);
            
            if (employee == null) {
                return "redirect:/admin/login?error=session_expired";
            }

            // Store profile picture URL in session for header display
            request.getSession().setAttribute("profilePictureUrl", employee.getProfilePictureUrl());
            
            model.addAttribute("employee", employee);
            model.addAttribute("pageTitle", "Chỉnh sửa hồ sơ");
            model.addAttribute("activeMenu", "profile");
            
            return "admin/profile/edit-profile";
            
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải form chỉnh sửa.");
            return "admin/profile/profile";
        }
    }

    /**
     * Process profile update
     */
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam @NotBlank @Size(max = 100) String fullName,
            @RequestParam @NotBlank @Email @Size(max = 100) String email,
            @RequestParam @NotBlank @Size(max = 20) String phoneNumber,
            @RequestParam @Size(max = 200) String address,
            @RequestParam(required = false) String dateOfBirth,
            @RequestParam(required = false) MultipartFile avatar,
            @RequestParam(required = false) String profilePictureUrl,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            // Kiểm tra xác thực
            if (authentication == null) {
                redirectAttributes.addFlashAttribute("error", "Phiên đăng nhập đã hết hạn.");
                return "redirect:/admin/login";
            }
            
            String username = authentication.getName();
            EmployeeDTO employee = employeeService.getEmployeeByUsername(username);
            
            if (employee == null) {
                redirectAttributes.addFlashAttribute("error", "Phiên đăng nhập đã hết hạn.");
                return "redirect:/admin/login";
            }

            // Handle avatar upload or profile picture URL
            String avatarPath = employee.getProfilePictureUrl();
            
            // Only process avatar if either a file is uploaded or a URL is provided
            boolean avatarFileUploaded = avatar != null && !avatar.isEmpty();
            boolean avatarUrlProvided = profilePictureUrl != null && !profilePictureUrl.trim().isEmpty();
            
            // If file is uploaded, it takes precedence over URL
            if (avatarFileUploaded) {
                try {
                    avatarPath = saveAvatar(avatar);
                } catch (IllegalArgumentException e) {
                    redirectAttributes.addFlashAttribute("error", e.getMessage());
                    return "redirect:/admin/profile/edit";
                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi lưu ảnh: " + e.getMessage());
                    return "redirect:/admin/profile/edit";
                }
            } 
            // If URL is provided and no file is uploaded
            else if (avatarUrlProvided) {
                // Validate URL format
                try {
                    // Kiểm tra nếu là URL bên ngoài
                    if (profilePictureUrl.startsWith("http")) {
                        new URL(profilePictureUrl);
                    }
                    avatarPath = profilePictureUrl;
                } catch (MalformedURLException e) {
                    // If URL is not valid, check if it might be a local file path
                    if (!profilePictureUrl.startsWith("/")) {
                        redirectAttributes.addFlashAttribute("error", "URL ảnh đại diện không hợp lệ.");
                        return "redirect:/admin/profile/edit";
                    }
                    avatarPath = profilePictureUrl;
                }
            }
            // If neither is provided, keep the existing avatar path

            // Parse date of birth
            Date birthDate = null;
            if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    sdf.setLenient(false); // Không chấp nhận ngày tháng không hợp lệ
                    birthDate = sdf.parse(dateOfBirth);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Định dạng ngày sinh không hợp lệ. Vui lòng sử dụng định dạng YYYY-MM-DD.");
                    return "redirect:/admin/profile/edit";
                }
            } else {
                // Giữ nguyên giá trị nếu không có thay đổi
                birthDate = employee.getDateOfBirth();
            }

            // Update employee information
            employee.setFullName(fullName);
            employee.setEmail(email);
            employee.setPhoneNumber(phoneNumber);
            employee.setAddress(address);
            employee.setDateOfBirth(birthDate);
            employee.setProfilePictureUrl(avatarPath);

            // Save to database
            boolean updateSuccess = employeeService.updateEmployeeProfile(employee);
            
            if (updateSuccess) {
                // Update session attribute after successful update
                request.getSession().setAttribute("profilePictureUrl", avatarPath);
                
                redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
                return "redirect:/admin/profile";
            } else {
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật thông tin.");
                return "redirect:/admin/profile/edit";
            }

        } catch (Exception e) {
            e.printStackTrace(); // Add logging for debugging
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/profile/edit";
        }
    }

    /**
     * Change password only
     */
    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam @NotBlank String currentPassword,
            @RequestParam @NotBlank @Size(min = 6, max = 100) String newPassword,
            @RequestParam @NotBlank String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            EmployeeDTO employee = employeeService.getEmployeeByUsername(username);
            
            if (employee == null) {
                redirectAttributes.addFlashAttribute("error", "Phiên đăng nhập đã hết hạn.");
                return "redirect:/admin/login";
            }

            // Validate current password
            if (!passwordEncoder.matches(currentPassword, employee.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng.");
                return "redirect:/admin/profile";
            }

            // Validate new password confirmation
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
                return "redirect:/admin/profile";
            }

            // Update password
            employee.setPassword(passwordEncoder.encode(newPassword));
            boolean updateSuccess = employeeService.updateEmployeeProfile(employee);
            
            if (updateSuccess) {
                redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi đổi mật khẩu.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/profile";
    }

    /**
     * Save uploaded avatar file
     */
    private String saveAvatar(MultipartFile file) throws IOException, IllegalArgumentException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ cho phép upload file ảnh");
        }

        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 5MB");
        }

        // Create upload directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/" + UPLOAD_DIR + filename;
    }
} 