package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.dto.common.response.CustomerDTO;
import com.hyperformancelabs.backend.model.Customer;
import com.hyperformancelabs.backend.repository.CustomerRepository;
import com.hyperformancelabs.backend.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public CustomerDTO getCustomerById(Integer customerId) {
        return customerRepository.findByCustomerId(customerId)
                .map(this::convertToCustomerDTO)
                .orElse(null);
    }

    @Override
    public CustomerDTO getCustomerByUsername(String username) {
        return customerRepository.findTopByUsernameOrderByCustomerIdDesc(username)
                .map(this::convertToCustomerDTO)
                .orElse(null);
    }

    @Override
    public void addCustomer(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        
        // Set basic required info
        customer.setName(customerDTO.getName());
        
        // Handle contact info - convert empty strings to null
        customer.setUsername(customerDTO.getUsername() != null && customerDTO.getUsername().trim().isEmpty() ? 
                            null : customerDTO.getUsername());
        customer.setEmail(customerDTO.getEmail() != null && customerDTO.getEmail().trim().isEmpty() ? 
                         null : customerDTO.getEmail());
        customer.setPhoneNumber(customerDTO.getPhoneNumber() != null && customerDTO.getPhoneNumber().trim().isEmpty() ? 
                               null : customerDTO.getPhoneNumber());
        
        // Validate at least one contact method is provided
        if (customer.getUsername() == null && customer.getEmail() == null && customer.getPhoneNumber() == null) {
            throw new IllegalArgumentException("Phải cung cấp ít nhất một trong các thông tin: tên đăng nhập, email hoặc số điện thoại");
        }
        
        customer.setPassword(customerDTO.getPassword());
        customer.setStreet(customerDTO.getStreet());
        customer.setWard(customerDTO.getWard());
        customer.setDistrict(customerDTO.getDistrict());
        customer.setCity(customerDTO.getCity());
        customer.setShippingNote(customerDTO.getShippingNote());
        customer.setNote(customerDTO.getNote());
        customer.setRating(0);
        customer.setStatus(customerDTO.getStatus());
        customer.setLoyaltyPoints(0);
        customer.setCreateAt(LocalDateTime.now());
        
        customerRepository.save(customer);
        convertToCustomerDTO(customer);
    }

    @Override
    public void updateCustomer(CustomerDTO customer) {
        Customer customerEntity = customerRepository.findByCustomerId(customer.getCustomerId()).orElse(null);
        if (customerEntity == null) {
            System.err.println("Không tìm thấy khách hàng với ID: " + customer.getCustomerId());
            return;
        }

        customerEntity.setName(customer.getName());
        
        // Handle contact info - convert empty strings to null
        customerEntity.setUsername(customer.getUsername() != null && customer.getUsername().trim().isEmpty() ? 
                                  null : customer.getUsername());
        customerEntity.setEmail(customer.getEmail() != null && customer.getEmail().trim().isEmpty() ? 
                               null : customer.getEmail());
        customerEntity.setPhoneNumber(customer.getPhoneNumber() != null && customer.getPhoneNumber().trim().isEmpty() ? 
                                     null : customer.getPhoneNumber());
        
        // Validate at least one contact method is provided
        if (customerEntity.getUsername() == null && customerEntity.getEmail() == null && customerEntity.getPhoneNumber() == null) {
            throw new IllegalArgumentException("Phải cung cấp ít nhất một trong các thông tin: tên đăng nhập, email hoặc số điện thoại");
        }
        
        customerEntity.setPassword(customer.getPassword());
        customerEntity.setStreet(customer.getStreet());
        customerEntity.setWard(customer.getWard());
        customerEntity.setDistrict(customer.getDistrict());
        customerEntity.setCity(customer.getCity());
        customerEntity.setShippingNote(customer.getShippingNote());
        customerEntity.setNote(customer.getNote());

        // Không để rating null
        if (customer.getRating() != null) {
            customerEntity.setRating(customer.getRating());
        }

        // loyaltyPoints cũng không được null
        if (customer.getLoyaltyPoints() != null) {
            customerEntity.setLoyaltyPoints(customer.getLoyaltyPoints());
        }

        customerEntity.setStatus(customer.getStatus());
        customerEntity.setUpdateAt(LocalDateTime.now());

        customerRepository.save(customerEntity);
    }

    @Override
    public void deleteCustomer(Integer customerId) {
        customerRepository.deleteById(customerId);
    }

    @Override
    public CustomerDTO getCustomerByEmailOrPhone(String email, String phone) {
        return customerRepository.findByEmailOrPhoneNumber(email, phone)
                .map(this::convertToCustomerDTO)
                .orElse(null);
    }

    @Override
    public CustomerDTO getCustomerByUsernameEmailOrPhone(String usernameEmailOrPhone) {
        return customerRepository.findByUsernameOrEmailOrPhoneNumber(usernameEmailOrPhone, usernameEmailOrPhone, usernameEmailOrPhone)
                .map(this::convertToCustomerDTO)
                .orElse(null);
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return customerRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return customerRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return customerRepository.existsByPhoneNumber(phone);
    }

    @Override
    public boolean existsByPhoneNumberAndStatusNot(String phone, String status) {
        return customerRepository.existsByPhoneNumberAndStatusNot(phone, status);
    }

    // -------------------------------------------------- ADMIN -----------------------------------------------------

    @Override
    public Long countNewCustomersToday(){
        return customerRepository.countCustomersToday();
    }

    @Override
    public Long countNewCustomersThisMonth(){
        return customerRepository.countCustomersThisMonth();
    }

    @Override
    public Long countNewCustomersThisYear(){
        return customerRepository.countCustomersThisYear();
    }

    @Override
    public Long countNewCustomersByMonthAndYear(int month, int year){
        return customerRepository.countCustomersByMonthAndYear(month, year);
    }

    @Override
    public Long countNewCustomersBetweenDates(LocalDate startDate, LocalDate endDate){
        return customerRepository.countCustomersBetweenDates(startDate, endDate);
    }

    @Override
    public void updateCustomerInfo(String customerId, String name, String email, String phoneNumber) {
        Customer customer = customerRepository.findByCustomerId(Integer.parseInt(customerId)).orElse(null);
        if (customer == null) {
            return;
        }
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhoneNumber(phoneNumber);
        customerRepository.save(customer);
    }

    @Override
    public Page<CustomerDTO> findCustomersWithOptionalStatusAndSort(String keyword, String status, String sortBy, String sortDir, Pageable pageable) {
        return customerRepository.findCustomersWithOptionalStatusAndSort(keyword, status, sortBy, sortDir, pageable)
                .map(this::convertToCustomerDTO);
    }

    private CustomerDTO convertToCustomerDTO(Customer customer) {
        return new CustomerDTO(
                customer.getCustomerId(),
                customer.getUsername(),
                customer.getPassword(),
                customer.getName(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                customer.getStreet(),
                customer.getWard(),
                customer.getDistrict(),
                customer.getCity(),
                customer.getShippingNote(),
                customer.getNote(),
                customer.getRating(),
                customer.getStatus(),
                customer.getLoyaltyPoints(),
                customer.getCreateAt(),
                customer.getUpdateAt()
        );
    }
}
