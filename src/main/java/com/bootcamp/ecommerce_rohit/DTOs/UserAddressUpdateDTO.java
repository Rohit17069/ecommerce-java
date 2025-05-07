package com.bootcamp.ecommerce_rohit.DTOs;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAddressUpdateDTO {
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
}
