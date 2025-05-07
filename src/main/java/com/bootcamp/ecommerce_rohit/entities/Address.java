package com.bootcamp.ecommerce_rohit.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Address extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;

    @Size(max = 100, message = "State must be at most 100 characters")
    private String state;

    @Size(max = 100, message = "Country must be at most 100 characters")
    private String country;

    @Size(max = 255, message = "Address Line must be at most 255 characters")
    private String addressLine;

    @Size(max = 10, message = "Zipcode must be at most 10 characters")
    private String zipCode;

    @Size(max = 50, message = "Label must be at most 50 characters")
    private String label;
    private Boolean is_Deleted=false;


    public Address(String city, String state, String country, String addressLine, String zipCode, String label) {
        this.city = city;
        this.state = state;
        this.country = country;
        this.addressLine = addressLine;
        this.zipCode = zipCode;
        this.label = label;
    }
}
