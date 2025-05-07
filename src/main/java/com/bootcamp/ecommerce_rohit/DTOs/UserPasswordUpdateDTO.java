package com.bootcamp.ecommerce_rohit.DTOs;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPasswordUpdateDTO {
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,15}$",
            message = "newPassword must contain at least one lowercase letter, one uppercase letter, one number, and one special character")
    String newPassword;
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,15}$",
            message = "confirmNewPassword must contain at least one lowercase letter, one uppercase letter, one number, and one special character")
    String confirmNewPassword;

}
