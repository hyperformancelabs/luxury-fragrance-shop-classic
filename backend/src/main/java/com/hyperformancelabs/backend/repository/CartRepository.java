package com.hyperformancelabs.backend.repository;

import com.hyperformancelabs.backend.model.Cart;
import com.hyperformancelabs.backend.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByCustomer_CustomerIdAndStatus(Integer customerId, String status);
    List<Cart> findByCustomerUsername(String username);
    // Lấy danh sách giỏ hàng của customer
    List<Cart> findByCustomer_Username(String username);

    // Lấy giỏ hàng active mới nhất
    Optional<Cart> findTopByCustomerAndStatusOrderByCartIdDesc(Customer customer, String status);
    Optional<Cart> findTopBySessionIdOrderByCartIdDesc(String sessionId);

}
