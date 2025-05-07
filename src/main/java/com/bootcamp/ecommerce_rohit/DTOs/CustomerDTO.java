package com.bootcamp.ecommerce_rohit.DTOs;

import com.bootcamp.ecommerce_rohit.entities.Address;
import com.bootcamp.ecommerce_rohit.entities.Role;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;
import java.util.UUID;


@Getter
@Setter
public class CustomerDTO {

    @NotBlank(message = "Email is required")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
            message = "Invalid email format"
    )
    private String email;

    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "companyContact must be of 10 digits.")
    private String contact;
    @NotBlank(message = "password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,15}$",
            message = "Password must contain at least one lowercase letter, one uppercase letter, one number, and one special character and of length between 8 and 15 characters")
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
    @Size(max = 15, message = "Middle name must not exceed 15 characters")
    private String middleName;
    private Role role;
    private List<Address> addresses;
}
