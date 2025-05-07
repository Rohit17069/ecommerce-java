package com.bootcamp.ecommerce_rohit.repositories;

import com.bootcamp.ecommerce_rohit.entities.Customer;
import com.bootcamp.ecommerce_rohit.entities.Seller;
import com.bootcamp.ecommerce_rohit.entities.User;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SellerRepository extends JpaRepository<Seller, UUID> {
  public   User findByGst(String gst);
  public  User findByCompanyName(String companyName);
  @Query("SELECT s FROM Seller s WHERE s.email LIKE %:emailFilterValue%")
  public Page<Seller> findByEmail(@Param("emailFilterValue") String emailFilterValue, Pageable pageable);
  public Seller findByEmail(String email);
  public Page<Seller> findAll(Pageable pageable);
  Seller findById(String sellerId);
  boolean existsByCompanyContact(String companyContact);
  boolean existsByCompanyNameIgnoreCase(String companyName);
  boolean existsByGst(String gst);

}
