package com.bootcamp.ecommerce_rohit.entities;


import jakarta.persistence.*;
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
public class Seller extends User{
    private String gst;
    private String companyContact;
    private String companyName;
@OneToMany(mappedBy = "seller")
   private List<Product>products=new ArrayList<>();

    public Seller(Address address, String email, String firstName, Role role, LocalDateTime passwordUpdateDate, Integer invalidAttemptCount, Boolean isLocked, Boolean isActive, Boolean isExpired, Boolean isDeleted, String password, String lastName, String middleName, String gst, String companyContact, String companyName, List<Product> products) {
        super(address,email,firstName,middleName,lastName,password,isDeleted,isActive,isExpired,isLocked,passwordUpdateDate,role);
        this.gst = gst;
        this.companyContact = companyContact;
        this.companyName = companyName;
        this.products = products;
    }
}