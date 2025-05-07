package com.bootcamp.ecommerce_rohit.DTOs;
import com.bootcamp.ecommerce_rohit.entities.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Getter
@Setter
public class SellerUpdateDTO {
    @Size(min = 3, max = 15, message = "First name must be between 3 and 15 characters")
    String firstName;
    @Size(max = 15, message = "Middle name must not exceed 15 characters")
    String middleName;
    @Size(min = 3, max = 15, message = "Last name must be between 3 and 15 characters")
    String lastName;
    @Pattern(
            regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$",
            message = "Invalid GST number format")
    String gst;
    String companyName;
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "companyContact must be of 10 digits.")
    String companyContact;
    String city;
    String state;
    String country;
    String addressLine;
    String zipCode;

    private MultipartFile profileImage;
}