package com.hyperformancelabs.backend.service;

import com.hyperformancelabs.backend.dto.CustomerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface CustomerService {

    CustomerDTO getCustomerById(Integer customerId);

    CustomerDTO getCustomerByUsername(String username);

    void addCustomer(CustomerDTO customerDTO);

    void updateCustomer(CustomerDTO customerDTO);

    void deleteCustomer(Integer customerId);

    CustomerDTO getCustomerByEmailOrPhone(String email, String phone);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phone);

    boolean existsByPhoneNumberAndStatusNot(String phone, String status);

    // --------------------------------------- ADMIN -----------------------------------------------------

    // Số khách hàng mới hôm nay
    Long countNewCustomersToday();

    // Số khách hàng mới trong tháng hiện tại
    Long countNewCustomersThisMonth();

    // Số khách hàng mới trong năm hiện tại
    Long countNewCustomersThisYear();

    // Số khách hàng mới theo tháng và năm
    Long countNewCustomersByMonthAndYear(int month, int year);

    // Số khách hàng mới theo ngày tháng năm
    Long countNewCustomersBetweenDates(LocalDate startDate, LocalDate endDate);

    // Cập nhật thông tin khách hàng
    void updateCustomerInfo(String customerId, String name, String email, String phoneNumber);

    // Lọc khách hàng
    Page<CustomerDTO> findCustomersWithOptionalStatusAndSort(String keyword, String status, String sortBy, String sortDir, Pageable pageable);
}
