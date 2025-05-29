package com.hyperformancelabs.backend.controller.admin;

import com.hyperformancelabs.backend.dto.EmployeeAdminDisplayDTO;
import com.hyperformancelabs.backend.dto.EmployeeDTO;
import com.hyperformancelabs.backend.service.EmployeeRoleService;
import com.hyperformancelabs.backend.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/employees")
public class AdminEmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRoleService employeeRoleService;

    @GetMapping
    public String listEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDir,
            Model model) {

        // Làm sạch tham số
        keyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        status = (status != null && !status.trim().isEmpty()) ? status.trim() : null;
        roleName = (roleName != null && !roleName.trim().isEmpty()) ? roleName.trim() : null;
        sortField = (sortField != null && !sortField.trim().isEmpty()) ? sortField.trim() : null;
        sortDir = (sortDir != null && !sortDir.trim().isEmpty()) ? sortDir.trim() : null;

        Pageable pageable = PageRequest.of(page, 10);
        Page<EmployeeAdminDisplayDTO> employeesPage = employeeService.findEmployeesWithFilters(
                status, roleName, keyword, sortField, sortDir, pageable
        );

        model.addAttribute("employees", employeesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeesPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("roleName", roleName);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

        return "admin/employees/list";
    }



    @GetMapping("/{id}")
    public String viewEmployee(@PathVariable("id") Integer id, Model model) {
        EmployeeDTO employee = employeeService.getEmployeeById(id);

        List<String> roles = employeeService.findActiveRoleNamesByEmployeeId(id);

        // Get role Order staff or Material staff
        List<String> filteredRoles = roles.stream()
                .filter(role -> role.equals("Order Staff") || role.equals("Material Staff"))
                .toList();

        model.addAttribute("employee", employee);
        model.addAttribute("employeeRoles", filteredRoles);

        return "admin/employees/detail";
    }

    @Transactional
    @PostMapping("/add")
    public String addEmployee(@ModelAttribute EmployeeAdminDisplayDTO dto, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Adding employee: " + dto);
            EmployeeDTO employee = employeeService.addEmployee(dto);
            employeeRoleService.addEmployeeRole(employee.getEmployeeId(), dto.getRole());
            redirectAttributes.addFlashAttribute("success", "Thêm nhân viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm nhân viên: " + e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    @Transactional
    @PostMapping("/update")
    public String updateEmployee(@ModelAttribute EmployeeDTO dto, RedirectAttributes redirectAttributes) {
        try {
            employeeService.updateEmployee(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nhân viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật nhân viên: " + e.getMessage());
        }
        return "redirect:/admin/employees/" + dto.getEmployeeId();
    }

    @Transactional
    @PostMapping("/delete")
    public String deleteEmployee(@RequestParam("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteEmployee(id);
            employeeRoleService.deleteEmployeeRoleByEmployeeId(id);
            redirectAttributes.addFlashAttribute("success", "Xóa nhân viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa nhân viên: " + e.getMessage());
        }
        return "redirect:/admin/employees";
    }

    private Map<String, Object> createSampleEmployee(int id, String username, String fullName, String phone, String email, String status) {
        Map<String, Object> employee = new HashMap<>();
        employee.put("id", id);
        employee.put("username", username);
        employee.put("fullName", fullName);
        employee.put("phoneNumber", phone);
        employee.put("email", email);
        employee.put("status", status);
        return employee;
    }
}
