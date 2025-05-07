package com.bootcamp.ecommerce_rohit.DTOs;


import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CustomerResponseDTO {
    String id;
    String fullName;
    String email;
    Boolean isActive;
String phoneNumber;
}
