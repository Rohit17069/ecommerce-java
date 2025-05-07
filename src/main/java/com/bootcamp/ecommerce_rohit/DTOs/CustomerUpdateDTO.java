package com.bootcamp.ecommerce_rohit.DTOs;

import jakarta.mail.Multipart;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CustomerUpdateDTO {
    @Size(min = 3, max = 15, message = "First name must be between 3 and 15 characters")
    String firstName;
    @Size(max = 15, message = "Middle name must not exceed 15 characters")
    String middleName;
    @Size(min = 3, max = 15, message = "Last name must be between 3 and 15 characters")
    String lastName;
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Contact must be of 10 digits.")
    private String contact;

    private MultipartFile profileImage;
}
