package com.bootcamp.ecommerce_rohit.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Customer extends User{
    private String  contact;

@OneToMany(mappedBy = "customer")
   private List<ProductReview>productReviews=new ArrayList<>();

@OneToMany(mappedBy = "customer")
private List<OrderTable> orderTables =new ArrayList<>();

@OneToMany(mappedBy = "customer")
private List<Cart>carts=new ArrayList<>();

    public Customer(Address address, String email, String firstName, Role role, LocalDateTime passwordUpdateDate, Integer invalidAttemptCount, Boolean isLocked, Boolean isActive, Boolean isExpired, Boolean isDeleted, String password, String lastName, String middleName, String contact, List<ProductReview> productReviews, List<OrderTable> orderTables, List<Cart> carts) {
  super(address,email,firstName,middleName,lastName,password,isDeleted,isActive,isExpired,isLocked,passwordUpdateDate,role);

        this.contact = contact;
        this.productReviews = productReviews;
        this.orderTables = orderTables;
        this.carts = carts;
    }
}
