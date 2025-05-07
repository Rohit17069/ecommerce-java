package com.bootcamp.ecommerce_rohit.DTOs;

import com.bootcamp.ecommerce_rohit.entities.Address;
import com.bootcamp.ecommerce_rohit.entities.Role;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;


@Getter
@Setter
public class SellerDTO {
    @NotBlank(message = "Email is required")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
            message = "Invalid email format"
    )
    private String email;
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "companyContact must be of 10 digits.")
    private String  companyContact;
    @NotBlank(message = "password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,15}$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, one number, and one special character")
    private String password;
    @NotBlank(message = "confirm password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,15}$",
            message = "confirmPassword must contain at least one lowercase letter, one uppercase letter, one number, and one special character")
    private String confirmPassword;
    @NotNull(message = "First name cannot be null")
    @NotBlank(message = "First name cannot be blank")
    @Size(min = 3, max = 15, message = "First name must be between 3 and 15 characters")
    private String firstName;

    @NotNull(message = "Last name cannot be null")
    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 3, max = 15, message = "First name must be between 3 and 15 characters")
    private String lastName;
    @NotBlank(message = "GST number is required")
    @Pattern(
            regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$",
            message = "Invalid GST number format")
    @NotBlank(message = "GST is required")
    private String gst;
    @NotBlank(message = "company name is required")
    private String companyName;
    private Address companyAddress;
private Role role;
}
