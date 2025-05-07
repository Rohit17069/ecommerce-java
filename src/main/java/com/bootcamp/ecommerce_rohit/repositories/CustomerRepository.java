package com.bootcamp.ecommerce_rohit.repositories;

import com.bootcamp.ecommerce_rohit.entities.Customer;
import com.bootcamp.ecommerce_rohit.entities.Seller;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    @Query("SELECT c FROM Customer c WHERE c.email LIKE %:emailFilterValue%")
    public Page<Customer> findByEmail(@Param("emailFilterValue") String emailFilterValue, Pageable pageable);
    public Page<Customer> findAll(Pageable pageable);
    Customer findById(String customerId);
    public Customer findByEmail(String email);
     boolean existsByContact(String contact);

}

